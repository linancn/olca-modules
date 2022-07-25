package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.ResultItemOrder;
import org.openlca.io.xls.results.CellWriter;

class ProcessFlowUpstreamSheet
		extends ContributionSheet<RootDescriptor, EnviFlow> {

	private final CellWriter writer;
	private final LcaResult r;
	private final ResultItemOrder items;

	static void write(ResultExport export, LcaResult r) {
		new ProcessFlowUpstreamSheet(export, r)
				.write(export.workbook);
	}

	private ProcessFlowUpstreamSheet(ResultExport export, LcaResult r) {
		super(export.writer, ResultExport.PROCESS_HEADER, ResultExport.FLOW_HEADER);
		this.writer = export.writer;
		this.r = r;
		this.items = export.items();
	}

	private void write(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Process upstream flows");
		header(sheet);
		subHeaders(sheet, items.processes(), items.enviFlows());
		data(sheet, items.processes(), items.enviFlows());
	}

	@Override
	protected double getValue(RootDescriptor process, EnviFlow flow) {
		return r.getUpstreamFlowResult(process, flow);
	}

	@Override
	protected void subHeaderCol(RootDescriptor process, Sheet sheet,
			int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(EnviFlow flow, Sheet sheet, int row) {
		writer.flowRow(sheet, row, 1, flow);
	}
}
