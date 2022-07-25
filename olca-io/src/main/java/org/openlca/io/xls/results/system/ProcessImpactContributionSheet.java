package org.openlca.io.xls.results.system;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.ResultItemOrder;
import org.openlca.io.xls.results.CellWriter;

class ProcessImpactContributionSheet
		extends ContributionSheet<RootDescriptor, ImpactDescriptor> {

	private final CellWriter writer;
	private final LcaResult r;
	private final ResultItemOrder items;

	static void write(ResultExport export, LcaResult r) {
		new ProcessImpactContributionSheet(export, r).write(export.workbook);
	}

	private ProcessImpactContributionSheet(ResultExport export, LcaResult r) {
		super(export.writer, ResultExport.PROCESS_HEADER,
				ResultExport.IMPACT_HEADER);
		this.writer = export.writer;
		this.r = r;
		this.items = export.items();
	}

	private void write(Workbook wb) {
		Sheet sheet = wb.createSheet("Process impact contributions");
		header(sheet);
		subHeaders(sheet, items.processes(), items.impacts());
		data(sheet, items.processes(), items.impacts());
	}

	@Override
	protected double getValue(RootDescriptor process, ImpactDescriptor impact) {
		return r.getDirectImpactResult(process, impact);
	}

	@Override
	protected void subHeaderCol(RootDescriptor process, Sheet sheet, int col) {
		writer.processCol(sheet, 1, col, process);
	}

	@Override
	protected void subHeaderRow(ImpactDescriptor impact, Sheet sheet, int row) {
		writer.impactRow(sheet, row, 1, impact);
	}

}
