package org.openlca.proto.io.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.CalculationTarget;
import org.openlca.core.model.CalculationType;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.providers.ResultProviders;
import org.openlca.proto.Proto;
import org.openlca.proto.grpc.ImpactFactorRequest;
import org.openlca.proto.grpc.ImpactFactorResponse;
import org.openlca.proto.grpc.Result;
import org.openlca.proto.grpc.ResultServiceGrpc;
import org.openlca.proto.grpc.ResultsProto;
import org.openlca.proto.grpc.TechFlowContributionRequest;
import org.openlca.proto.io.input.In;
import org.openlca.proto.io.output.Refs;
import org.openlca.util.Pair;
import org.openlca.util.Strings;

import com.google.protobuf.Empty;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

class ResultService extends ResultServiceGrpc.ResultServiceImplBase {

  final IDatabase db;
  final Map<String, FullResult> results = new HashMap<>();

  ResultService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void calculate(
    Proto.CalculationSetup req, StreamObserver<Result> resp) {
    var p = setup(req);
    if (p.first == null) {
      resp.onError(Status.INVALID_ARGUMENT
        .withDescription(p.second)
        .asException());
      return;
    }

    var setup = p.first;
    var data = MatrixData.of(db, setup);
    var provider = ResultProviders.lazyOf(db, data);
    var result = new FullResult(provider);

    var key = UUID.randomUUID().toString();
    results.put(key, result);
    var r = Result.newBuilder()
      .setId(key)
      .build();
    resp.onNext(r);
    resp.onCompleted();
  }

  private Pair<CalculationSetup, String> setup(Proto.CalculationSetup proto) {
    var target = targetOf(proto);
    if (target == null)
      return Pair.of(null, "Product system or process does not exist");

    // initialize the setup
    var type = switch (proto.getCalculationType()) {
      case MONTE_CARLO_SIMULATION -> CalculationType.MONTE_CARLO_SIMULATION;
      case SIMPLE_CALCULATION -> CalculationType.SIMPLE_CALCULATION;
      case UPSTREAM_ANALYSIS -> CalculationType.UPSTREAM_ANALYSIS;
      default -> CalculationType.CONTRIBUTION_ANALYSIS;
    };
    var setup = new CalculationSetup(type, target);

    // demand value
    if (proto.getAmount() != 0) {
      setup.withAmount(proto.getAmount());
    }

    // flow property
    var qref = refExchangeOf(target);
    var propID = proto.getFlowProperty().getId();
    if (Strings.notEmpty(propID)
      && qref != null
      && qref.flow != null) {
      qref.flow.flowPropertyFactors.stream()
        .filter(f -> Strings.nullOrEqual(propID, f.flowProperty.refId))
        .findAny()
        .ifPresent(setup::withFlowPropertyFactor);
    }

    // unit
    var unitID = proto.getUnit().getId();
    var propFac = setup.flowPropertyFactor();
    if (Strings.notEmpty(unitID)
      && propFac != null
      && propFac.flowProperty != null
      && propFac.flowProperty.unitGroup != null) {
      var group = propFac.flowProperty.unitGroup;
      group.units.stream()
        .filter(u -> Strings.nullOrEqual(unitID, u.refId))
        .findAny()
        .ifPresent(setup::withUnit);
    }

    // impact method and NW set
    var methodID = proto.getImpactMethod().getId();
    if (Strings.notEmpty(methodID)) {
      setup.withImpactMethod(db.get(ImpactMethod.class, methodID));
      var nwID = proto.getNwSet().getId();
      if (Strings.notEmpty(nwID) && setup.impactMethod() != null) {
        for (var nwSet : setup.impactMethod().nwSets) {
          if (nwID.equals(nwSet.refId)) {
            setup.withNwSet(nwSet);
            break;
          }
        }
      }
    }

    // other settings
    setup.withAllocation(In.allocationMethod(proto.getAllocationMethod()))
      .withCosts(proto.getWithCosts())
      .withRegionalization(proto.getWithRegionalization());

    // add parameter redefinitions
    var protoRedefs = proto.getParameterRedefsList();
    if (protoRedefs.isEmpty() && target.isProductSystem()) {
      target.asProductSystem()
        .parameterSets.stream()
        .filter(set -> set.isBaseline)
        .findAny()
        .ifPresent(set -> setup.withParameters(set.parameters));
    } else if (!protoRedefs.isEmpty()) {
      var params = new ArrayList<ParameterRedef>();
      for (var protoRedef : protoRedefs) {
        params.add(In.parameterRedefOf(protoRedef, db));
      }
      setup.withParameters(params);
    }

    return Pair.of(setup, null);
  }

  private CalculationTarget targetOf(Proto.CalculationSetup proto) {
    var refID = proto.getProductSystem().getId();
    if (Strings.nullOrEmpty(refID))
      return null;
    var system = db.get(ProductSystem.class, refID);
    if (system != null)
      return system;
    return db.get(Process.class, refID);
  }

  private Exchange refExchangeOf(CalculationTarget target) {
    if (target == null)
      return null;
    if (target.isProcess()) {
      var p = target.asProcess();
      return p.quantitativeReference;
    } else if (target.isProductSystem()) {
      var s = target.asProductSystem();
      return s.referenceExchange;
    }
    return null;
  }

  @Override
  public void getTechFlows(Result req, StreamObserver<ResultsProto.TechFlow> resp) {
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var refData = Refs.dataOf(db);
    for (var product : result.techIndex()) {
      resp.onNext(Results.toProto(product, refData));
    }
    resp.onCompleted();
  }

  @Override
  public void getEnviFlows(Result req, StreamObserver<ResultsProto.EnviFlow> resp) {
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var flows = result.enviIndex();
    if (flows == null) {
      resp.onCompleted();
      return;
    }
    var refData = Refs.dataOf(db);
    for (var flow : flows) {
      resp.onNext(Results.toProto(flow, refData));
    }
    resp.onCompleted();
  }

  @Override
  public void getImpactCategories(Result req, StreamObserver<Proto.Ref> resp) {
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var impacts = result.impactIndex();
    if (impacts == null)
      return;
    var refData = Refs.dataOf(db);
    for (var impact : impacts) {
      resp.onNext(Refs.refOf(impact, refData).build());
    }
    resp.onCompleted();
  }

  @Override
  public void getTotalInventory(
    Result req, StreamObserver<ResultsProto.ResultValue> resp) {

    // TODO maybe wrap with `withResult`
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var flows = result.enviIndex();
    if (flows == null) {
      resp.onCompleted();
      return;
    }

    var refData = Refs.dataOf(db);
    for (var flow : flows) {
      var value = result.getTotalFlowResult(flow);
      if (value == 0)
        continue;
      resp.onNext(Results.toProtoResult(flow, refData, value));
    }
    resp.onCompleted();
  }

  @Override
  public void getTotalImpacts(
    Result req, StreamObserver<ResultsProto.ResultValue> resp) {

    // get the impact results
    var result = results.get(req.getId());
    if (result == null) {
      resp.onCompleted();
      return;
    }
    var impacts = result.impactIndex();
    if (impacts == null) {
      resp.onCompleted();
      return;
    }

    var refData = Refs.dataOf(db);
    for (var impact : impacts) {
      var value = result.getTotalImpactResult(impact);
      var proto = ResultsProto.ResultValue.newBuilder()
        .setImpact(Refs.refOf(impact, refData))
        .setValue(value)
        .build();
      resp.onNext(proto);
    }
    resp.onCompleted();
  }

  @Override
  public void getImpactFactors(
    ImpactFactorRequest req, StreamObserver<ImpactFactorResponse> resp) {

    // check that we have a result with  flows and impacts
    var result = results.get(req.getResult().getId());
    if (result == null) {
      resp.onError(Status.INVALID_ARGUMENT
        .withDescription("Invalid result ID")
        .asException());
      return;
    }
    var flowIndex = result.enviIndex();
    var impactIndex = result.impactIndex();
    if (flowIndex == null || impactIndex == null) {
      resp.onCompleted();
      return;
    }

    // check that we have at least an indicator or flow
    var indicator = Results.findImpact(result, req.getIndicator());
    var flow = Results.findFlow(result, req.getFlow());
    if (flow == null && indicator == null) {
      resp.onCompleted();
      return;
    }

    var refData = Refs.dataOf(db);

    // get one specific factor of an indicator and flow
    if (indicator != null && flow != null) {
      var factor = ImpactFactorResponse.newBuilder()
        .setIndicator(Refs.refOf(indicator))
        .setFlow(Results.toProto(flow, refData))
        .setValue(result.getImpactFactor(indicator, flow));
      resp.onNext(factor.build());
      resp.onCompleted();
      return;
    }

    // get non-zero factors of an indicator
    if (flow == null) {
      var indicatorRef = Refs.refOf(indicator);
      for (var iFlow : flowIndex) {
        var value = result.getImpactFactor(indicator, iFlow);
        if (value == 0)
          continue;
        var factor = ImpactFactorResponse.newBuilder()
          .setIndicator(indicatorRef)
          .setFlow(Results.toProto(iFlow, refData))
          .setValue(value);
        resp.onNext(factor.build());
      }
      resp.onCompleted();
      return;
    }

    // get all impact factors of a flow
    for (var impact : impactIndex) {
      var factor = ImpactFactorResponse.newBuilder()
        .setIndicator(Refs.refOf(impact))
        .setFlow(Results.toProto(flow, refData))
        .setValue(result.getImpactFactor(impact, flow));
      resp.onNext(factor.build());
    }
    resp.onCompleted();
  }

  @Override
  public void getDirectContribution(
    TechFlowContributionRequest req,
    StreamObserver<ResultsProto.ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact(FullResult::getDirectImpactResult)
      .ifFlow(FullResult::getDirectFlowResult)
      .ifCosts(FullResult::getDirectCostResult)
      .close();
  }

  @Override
  public void getTotalContribution(
    TechFlowContributionRequest req,
    StreamObserver<ResultsProto.ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact(FullResult::getUpstreamImpactResult)
      .ifFlow(FullResult::getUpstreamFlowResult)
      .ifCosts(FullResult::getUpstreamCostResult)
      .close();
  }

  @Override
  public void getTotalContributionOfOne(
    TechFlowContributionRequest req,
    StreamObserver<ResultsProto.ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact((result, product, impact) -> {
        var productIdx = result.techIndex().of(product);
        var impactIdx = result.impactIndex().of(impact);
        return result.provider.totalImpactOfOne(impactIdx, productIdx);
      })
      .ifFlow((result, product, flow) -> {
        var productIdx = result.techIndex().of(product);
        var flowIdx = result.enviIndex().of(flow);
        var value = result.provider.totalFlowOfOne(flowIdx, productIdx);
        return result.adopt(flow, value);
      })
      .ifCosts((result, product) -> {
        var productIdx = result.techIndex().of(product);
        return result.provider.totalCostsOfOne(productIdx);
      })
      .close();
  }

  @Override
  public void dispose(Result req, StreamObserver<Empty> resp) {
    results.remove(req.getId());
    // we always return ok, even when the result does not exist
    resp.onNext(Empty.newBuilder().build());
    resp.onCompleted();
  }

}
