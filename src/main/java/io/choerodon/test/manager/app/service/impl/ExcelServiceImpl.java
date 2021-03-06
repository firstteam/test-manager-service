package io.choerodon.test.manager.app.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.test.manager.api.dto.TestCycleCaseDTO;
import io.choerodon.test.manager.api.dto.TestCycleDTO;
import io.choerodon.test.manager.app.service.ExcelService;
import io.choerodon.test.manager.app.service.TestCaseService;
import io.choerodon.test.manager.app.service.TestCycleCaseService;
import io.choerodon.test.manager.app.service.TestCycleService;
import io.choerodon.test.manager.domain.service.IExcelService;
import io.choerodon.test.manager.domain.service.impl.ICycleCaseExcelServiceImpl;
import io.choerodon.test.manager.domain.test.manager.entity.TestCycleE;
import io.choerodon.test.manager.domain.test.manager.factory.TestCycleEFactory;
import io.choerodon.test.manager.infra.common.utils.ExcelUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 842767365@qq.com on 8/9/18.
 */

@Component
public class ExcelServiceImpl implements ExcelService {

	private static final String EXPORT_ERROR = "error.issue.export";
	private static final String EXPORT_ERROR_WORKBOOK_CLOSE = "error.issue.close.workbook";
	private static final String EXPORT_ERROR_SET_HEADER = "error.issue.set.header";

	Log log = LogFactory.getLog(this.getClass());
	@Autowired
	TestCycleCaseService testCycleCaseService;

	@Autowired
	IExcelService iExcelService;

	@Autowired
	TestCycleService testCycleService;

	@Autowired
	TestCaseService testCaseService;


	/**
	 * 设置http请求报文为下载文件
	 *
	 * @param response
	 * @param request
	 * @param fileName
	 * @throws UnsupportedEncodingException
	 **/
	private void setExcelHeader(HttpServletResponse response, HttpServletRequest request, String fileName) {
		String charsetName = "UTF-8";
		if (request.getHeader("User-Agent").contains("Firefox")) {
			charsetName = "GB2312";
		}

		response.reset();
		response.setContentType("application/ms-excel;charset=utf-8");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String((fileName + ".xls").getBytes(charsetName),
					"ISO-8859-1"));
		} catch (UnsupportedEncodingException e1) {
			throw new CommonException(EXPORT_ERROR_SET_HEADER,e1);
		}
		response.setCharacterEncoding("utf-8");

	}


	/**
	 * 导出一个cycle下的测试详情，默认HSSFWorkBook
	 *
	 * @param cycleId
	 * @param projectId
	 */
	@Override
	public void exportCycleCaseInOneCycle(Long cycleId, Long projectId, HttpServletRequest request,
										  HttpServletResponse response) {
		setExcelHeader(response, request, "fileName");


		Assert.notNull(cycleId, "error.export.cycle.in.one.cycleId.not.be.null");
		TestCycleE cycleE = TestCycleEFactory.create();
		cycleE.setCycleId(cycleId);
		Long[] cycleIds = Stream.concat(cycleE.getChildFolder().stream().map(TestCycleE::getCycleId), Stream.of(cycleId)).toArray(Long[]::new);
		TestCycleDTO cycle = ConvertHelper.convert(cycleE.queryOne(), TestCycleDTO.class);
		testCycleService.populateVersion(cycle, projectId);
		testCycleService.populateUsers(Lists.newArrayList(cycle));
		Map<Long, List<TestCycleCaseDTO>> cycleCaseMap = Optional.ofNullable(testCycleCaseService.queryCaseAllInfoInCyclesOrVersions(cycleIds, null, projectId))
				.orElseGet(ArrayList::new).stream().collect(Collectors.groupingBy(TestCycleCaseDTO::getCycleId));
		IExcelService service=new <TestCycleDTO,TestCycleCaseDTO> ICycleCaseExcelServiceImpl();
		Workbook workbook = service.exportWorkBook(cycleCaseMap, testCaseService.getProjectInfo(projectId).getName(), cycle);
		downloadWorkBook(workbook, response);
	}


	private void downloadWorkBook(Workbook workbook, HttpServletResponse response) {
		try {
			workbook.write(response.getOutputStream());
		} catch (IOException e) {
			throw new CommonException(EXPORT_ERROR,e);
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				log.warn(EXPORT_ERROR_WORKBOOK_CLOSE,e);
			}
		}
	}


}
