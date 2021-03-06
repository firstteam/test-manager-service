package io.choerodon.test.manager.app.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.choerodon.agile.api.dto.IssueCommonDTO;
import io.choerodon.agile.api.dto.ProductVersionDTO;
import io.choerodon.agile.api.dto.SearchDTO;
import io.choerodon.agile.api.dto.UserDO;
import io.choerodon.agile.infra.common.utils.RankUtil;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.test.manager.api.dto.IssueInfosDTO;
import io.choerodon.test.manager.api.dto.TestCycleCaseDTO;
import io.choerodon.test.manager.api.dto.TestCycleCaseDefectRelDTO;
import io.choerodon.test.manager.api.dto.TestCycleDTO;
import io.choerodon.test.manager.app.service.*;
import io.choerodon.test.manager.domain.service.ITestCycleCaseService;
import io.choerodon.test.manager.domain.service.ITestCycleService;
import io.choerodon.test.manager.domain.test.manager.entity.TestCycleCaseE;
import io.choerodon.test.manager.domain.test.manager.entity.TestCycleE;
import io.choerodon.test.manager.domain.test.manager.entity.TestStatusE;
import io.choerodon.test.manager.domain.test.manager.factory.TestCycleCaseEFactory;
import io.choerodon.test.manager.domain.test.manager.factory.TestCycleEFactory;
import io.choerodon.test.manager.infra.feign.ProductionVersionClient;
import io.choerodon.test.manager.infra.feign.TestCaseFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 842767365@qq.com on 6/11/18.
 */
@Component
public class TestCycleCaseServiceImpl implements TestCycleCaseService {
    @Autowired
    ITestCycleCaseService iTestCycleCaseService;

    @Autowired
    ITestCycleService iTestCycleService;

    @Autowired
    TestCaseFeignClient testCaseFeignClient;

    @Autowired
    TestCaseService testCaseService;

    @Autowired
    ProductionVersionClient productionVersionClient;

    @Autowired
    TestCycleCaseDefectRelService testCycleCaseDefectRelService;

    @Autowired
    UserService userService;

    @Autowired
    TestStatusService testStatusService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long cycleCaseId, Long projectId) {
        TestCycleCaseDTO dto = new TestCycleCaseDTO();
        dto.setExecuteId(cycleCaseId);
        iTestCycleCaseService.delete(ConvertHelper.convert(dto, TestCycleCaseE.class));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDelete(TestCycleCaseDTO testCycleCaseDTO, Long projectId) {
        List<TestCycleCaseE> list=ConvertHelper.convert(testCycleCaseDTO,TestCycleCaseE.class).querySelf();
        if(!ObjectUtils.isEmpty(list)) {
            list.forEach(v->delete(v.getExecuteId(),projectId));
        }
    }

    @Override
    public Page<TestCycleCaseDTO> queryByCycle(TestCycleCaseDTO dto, PageRequest pageRequest, Long projectId) {
        TestCycleDTO queryTestCycleDTO = new TestCycleDTO();
        queryTestCycleDTO.setCycleId(dto.getCycleId());
        //找到所有的子阶段
        List<TestCycleE> testCycleES = iTestCycleService.queryChildCycle(ConvertHelper.convert(queryTestCycleDTO, TestCycleE.class));
        //装配值进DTO中
        List<TestCycleCaseDTO> testCycleCaseDTOS = new ArrayList<>();
        testCycleCaseDTOS.add(dto);
        testCycleES.forEach(v -> {
                    TestCycleCaseDTO tempDTO = new TestCycleCaseDTO();
                    tempDTO.setAssignedTo(dto.getAssignedTo());
                    tempDTO.setLastUpdatedBy(dto.getLastUpdatedBy());
                    tempDTO.setCycleId(v.getCycleId());
                    tempDTO.setCycleName(v.getCycleName());
                    testCycleCaseDTOS.add(tempDTO);
                }
        );

        Page<TestCycleCaseE> serviceEPage = iTestCycleCaseService.queryByFatherCycle(ConvertHelper.convertList(testCycleCaseDTOS, TestCycleCaseE.class), pageRequest);
        Page<TestCycleCaseDTO> dots = ConvertPageHelper.convertPage(serviceEPage, TestCycleCaseDTO.class);

        populateCycleCaseWithDefect(dots, projectId);
        populateUsers(dots);
        return dots;
    }

    @Override
    public Page<TestCycleCaseDTO> queryByCycleWithFilterArgs(Long cycleId, PageRequest pageRequest, Long projectId, TestCycleCaseDTO searchDTO) {
        searchDTO = Optional.ofNullable(searchDTO).orElseGet(TestCycleCaseDTO::new);
        searchDTO.setCycleId(cycleId);
        Page<TestCycleCaseE> serviceEPage = iTestCycleCaseService.query(ConvertHelper.convert(searchDTO, TestCycleCaseE.class), pageRequest);
        Page<TestCycleCaseDTO> dots = ConvertPageHelper.convertPage(serviceEPage, TestCycleCaseDTO.class);

        populateUsers(dots);
        return dots;
    }

    @Override
    public List<TestCycleCaseDTO> queryByIssuse(Long issuseId, Long projectId) {

        List<TestCycleCaseDTO> dto = ConvertHelper.convertList(iTestCycleCaseService.queryByIssue(issuseId), TestCycleCaseDTO.class);
        if (ObjectUtils.isEmpty(dto)) {
            return new ArrayList<>();
        }
        populateCycleCaseWithDefect(dto, projectId);
        populateUsers(dto);
        populateVersionBuild(projectId, dto);
        return dto;
    }


    /**
     * 查询issues的cycleCase 在生成报表处使用
     *
     * @param issueIds
     * @param projectId
     * @return
     */
    @Override
    public List<TestCycleCaseDTO> queryInIssues(Long[] issueIds, Long projectId) {
        if (issueIds == null || issueIds.length == 0) {
            return new ArrayList<>();
        }
        List<TestCycleCaseDTO> dto = ConvertHelper.convertList(iTestCycleCaseService.queryInIssues(issueIds), TestCycleCaseDTO.class);
        if (dto == null || dto.isEmpty()) {
            return new ArrayList<>();
        }
        populateCycleCaseWithDefect(dto, projectId);
        return dto;
    }

    @Override
    public List<TestCycleCaseDTO> queryCaseAllInfoInCyclesOrVersions(Long[] cycleIds, Long[] versionIds, Long projectId) {
        Assert.notNull(cycleIds, "error.query.case.in.versions.project.not.be.null");

        List<TestCycleCaseDTO> dto = ConvertHelper.convertList(iTestCycleCaseService.queryCaseAllInfoInCyclesOrVersions(cycleIds, versionIds), TestCycleCaseDTO.class);
        populateCycleCaseWithDefect(dto, projectId);
        populateUsers(dto);
        return dto;
    }

    /**
     * 将实例查询的Issue信息和缺陷关联的Issue信息合并到一起，为了减少一次外部调用。
     *
     * @param testCycleCaseDTOS
     * @param projectId
     */
    private void populateCycleCaseWithDefect(List<TestCycleCaseDTO> testCycleCaseDTOS, Long projectId) {
        List<TestCycleCaseDefectRelDTO> list = new ArrayList<>();
        for (TestCycleCaseDTO v : testCycleCaseDTOS) {
            List<TestCycleCaseDefectRelDTO> defects = v.getDefects();
            Optional.ofNullable(defects).ifPresent(list::addAll);
            Optional.ofNullable(v.getSubStepDefects()).ifPresent(list::addAll);
        }

        Long[] issueLists = Stream.concat(list.stream().map(TestCycleCaseDefectRelDTO::getIssueId),
                testCycleCaseDTOS.stream().map(TestCycleCaseDTO::getIssueId)).filter(Objects::nonNull).distinct()
                .toArray(Long[]::new);
        if (ObjectUtils.isEmpty(issueLists)) {
            return;
        }
        Map<Long, IssueInfosDTO> defectMap = testCaseService.getIssueInfoMap(projectId, issueLists, true);

        list.forEach(v -> v.setIssueInfosDTO(defectMap.get(v.getIssueId())));
        testCycleCaseDTOS.forEach(v -> v.setIssueInfosDTO(defectMap.get(v.getIssueId())));
    }


    private void populateVersionBuild(Long projectId, List<TestCycleCaseDTO> dto) {
        Map<Long, ProductVersionDTO> map = testCaseService.getVersionInfo(projectId);
        if (ObjectUtils.isEmpty(map)) {
            return;
        }
        TestCycleE cycleE = TestCycleEFactory.create();

        for (TestCycleCaseDTO cases : dto) {
            cycleE.setCycleId(cases.getCycleId());
            Long versionId = cycleE.queryOne().getVersionId();
            Assert.notNull(versionId, "error.version.id.not.null");
            Optional.ofNullable(map.get(versionId)).ifPresent(v -> cases.setVersionName(v.getName()));
        }

    }

    @Override
    public TestCycleCaseDTO queryOne(Long cycleCaseId, Long projectId) {
        TestCycleCaseDTO testCycleCaseDTO = new TestCycleCaseDTO();
        testCycleCaseDTO.setExecuteId(cycleCaseId);
        TestCycleCaseDTO dto = ConvertHelper.convert(iTestCycleCaseService.queryOne(ConvertHelper.convert(testCycleCaseDTO, TestCycleCaseE.class)), TestCycleCaseDTO.class);
        testCycleCaseDefectRelService.populateDefectAndIssue(dto, projectId);
        userService.populateTestCycleCaseDTO(dto);
        return dto;
    }

    private void populateUsers(List<TestCycleCaseDTO> users) {
        List<Long> usersId = new ArrayList<>();
        users.stream().forEach(v -> {
            Optional.ofNullable(v.getAssignedTo()).ifPresent(usersId::add);
            Optional.ofNullable(v.getLastUpdatedBy()).ifPresent(usersId::add);
        });
        List<Long> ids = usersId.stream().distinct().filter(v -> !v.equals(Long.valueOf(0))).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(ids)) {
            Map<Long, UserDO> userMaps = userService.query(ids.toArray(new Long[ids.size()]));
            users.forEach(v -> {
                Optional.ofNullable(userMaps.get(v.getAssignedTo())).ifPresent(v::setAssigneeUser);
                Optional.ofNullable(userMaps.get(v.getLastUpdatedBy())).ifPresent(v::setLastUpdateUser);

            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TestCycleCaseDTO create(TestCycleCaseDTO testCycleCaseDTO, Long projectId) {
        testCycleCaseDTO.setExecutionStatus(testStatusService.getDefaultStatusId(TestStatusE.STATUS_TYPE_CASE));
        testCycleCaseDTO.setLastRank(TestCycleCaseEFactory.create().getLastedRank(testCycleCaseDTO.getCycleId()));
        return ConvertHelper.convert(iTestCycleCaseService.runTestCycleCase(ConvertHelper.convert(testCycleCaseDTO, TestCycleCaseE.class), projectId), TestCycleCaseDTO.class);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public TestCycleCaseDTO changeOneCase(TestCycleCaseDTO testCycleCaseDTO, Long projectId) {
        testStatusService.populateStatus(testCycleCaseDTO);
        TestCycleCaseDTO dto=ConvertHelper.convert(iTestCycleCaseService.changeStep(ConvertHelper.convert(testCycleCaseDTO, TestCycleCaseE.class)), TestCycleCaseDTO.class);
        userService.populateTestCycleCaseDTO(dto);
        return dto;
    }


    @Override
    public List<Long> getActiveCase(Long range, Long projectId, String day) {
        return iTestCycleCaseService.getActiveCase(range, projectId, day);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createFilteredCycleCaseInCycle(Long projectId, Long fromCycleId, Long toCycleId, Long assignee, SearchDTO searchDTO) {
        TestCycleCaseE testCycleCaseE = TestCycleCaseEFactory.create();
        testCycleCaseE.setCycleId(fromCycleId);
        Map filterMap = new HashMap();
        Optional.ofNullable(searchDTO.getExecutionStatus()).ifPresent(v -> filterMap.put("executionStatus", v));

        List<TestCycleCaseE> testCycleCaseES = testCycleCaseE.filter(filterMap);
        if (!(ObjectUtils.isEmpty(testCycleCaseES))) {
            List<TestCycleCaseDTO> testCycleCase = ConvertHelper.convertList(testCycleCaseES, TestCycleCaseDTO.class);
            testCycleCaseDefectRelService.populateCycleCaseDefectInfo(testCycleCase, projectId);

            Map idMap = new HashMap();
            Object[] ids = testCycleCase.stream().map(TestCycleCaseDTO::getIssueId).toArray();
            idMap.put("issueIds", ids);
            searchDTO.setOtherArgs(idMap);
            ResponseEntity<Page<IssueCommonDTO>> responseEntity = testCaseService.listIssueWithoutSub(projectId, searchDTO, new PageRequest(0, 9999999));
            Set issueListDTOS = responseEntity.getBody().stream().map(IssueCommonDTO::getIssueId).collect(Collectors.toSet());

            Long defaultStatus = testStatusService.getDefaultStatusId(TestStatusE.STATUS_TYPE_CASE);
            final String[] lastRank = new String[1];
            lastRank[0] = testCycleCaseE.getLastedRank(testCycleCaseE.getCycleId());
            String[] defectStatus = searchDTO.getDefectStatus();
            Set<String> defectSets = Sets.newHashSet();
            if (!ObjectUtils.isEmpty(defectStatus)) {
                for (String de : defectStatus) {
                    defectSets.add(de);
                }
            }
            testCycleCase.stream().filter(v -> issueListDTOS.contains(v.getIssueId()) && containsDefect(defectSets, v.getDefects()))
                    .forEach(u -> {
                        u.setExecuteId(null);
                        u.setRank(RankUtil.Operation.INSERT.getRank(lastRank[0], null));
                        u.setAssignedTo(assignee);
                        u.setCycleId(toCycleId);
                        u.setExecutionStatus(defaultStatus);
                        u.setObjectVersionNumber(Long.valueOf(0));
                        lastRank[0] = iTestCycleCaseService.cloneCycleCase(ConvertHelper.convert(u, TestCycleCaseE.class), projectId).getRank();
                    });

        }

        return true;
    }

    private boolean containsDefect(Set<String> defectSet, List<TestCycleCaseDefectRelDTO> defects) {
        if (defectSet.isEmpty()) {
            return true;
        }
        if (ObjectUtils.isEmpty(defects)) {
            return false;
        }

        for (TestCycleCaseDefectRelDTO v : defects) {
            if (v.getIssueInfosDTO() != null && defectSet.contains(v.getIssueInfosDTO().getStatusCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Long countCaseNotRun(Long projectId) {
        return iTestCycleCaseService.countCaseNotRun(projectId);
    }

    @Override
    public Long countCaseNotPlain(Long projectId) {
        return iTestCycleCaseService.countCaseNotPlain(projectId);
    }

    @Override
    public Long countCaseSum(Long projectId) {
        return iTestCycleCaseService.countCaseSum(projectId);
    }


}
