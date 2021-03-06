package io.choerodon.agile.api.dto;

import io.choerodon.test.manager.api.dto.TestCaseStepDTO;
import io.choerodon.agile.infra.common.utils.StringUtil;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author dinghuang123@gmail.com
 */
public class IssueCreateDTO {

    private String issueNum;

    private String typeCode;

    private String summary;

    private String priorityCode;

    private String description;

    private Long assigneeId;

    private Long projectId;

    private Long epicId;

    private Long sprintId;

    private Integer storyPoints;

    @Transient
    private List<TestCaseStepDTO> testCaseStepDTOS;

    private List<VersionIssueRelDTO> versionIssueRelDTOList;

    private List<LabelIssueRelDTO> labelIssueRelDTOList;

    private List<ComponentIssueRelDTO> componentIssueRelDTOList;

    private List<IssueLinkDTO> issueLinkDTOList;

    private BigDecimal remainingTime;

    private BigDecimal estimateTime;

    private String epicName;

    public String getIssueNum() {
        return issueNum;
    }

    public void setIssueNum(String issueNum) {
        this.issueNum = issueNum;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPriorityCode() {
        return priorityCode;
    }

    public void setPriorityCode(String priorityCode) {
        this.priorityCode = priorityCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    public List<VersionIssueRelDTO> getVersionIssueRelDTOList() {
        return versionIssueRelDTOList;
    }

    public void setVersionIssueRelDTOList(List<VersionIssueRelDTO> versionIssueRelDTOList) {
        this.versionIssueRelDTOList = versionIssueRelDTOList;
    }

    public List<LabelIssueRelDTO> getLabelIssueRelDTOList() {
        return labelIssueRelDTOList;
    }

    public void setLabelIssueRelDTOList(List<LabelIssueRelDTO> labelIssueRelDTOList) {
        this.labelIssueRelDTOList = labelIssueRelDTOList;
    }

    public List<ComponentIssueRelDTO> getComponentIssueRelDTOList() {
        return componentIssueRelDTOList;
    }

    public void setComponentIssueRelDTOList(List<ComponentIssueRelDTO> componentIssueRelDTOList) {
        this.componentIssueRelDTOList = componentIssueRelDTOList;
    }

    public List<IssueLinkDTO> getIssueLinkDTOList() {
        return issueLinkDTOList;
    }

    public void setIssueLinkDTOList(List<IssueLinkDTO> issueLinkDTOList) {
        this.issueLinkDTOList = issueLinkDTOList;
    }

    public BigDecimal getRemainingTime() {
        return remainingTime;
    }

    public BigDecimal getEstimateTime() {
        return estimateTime;
    }

    public void setEstimateTime(BigDecimal estimateTime) {
        this.estimateTime = estimateTime;
    }

    public void setRemainingTime(BigDecimal remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getEpicName() {
        return epicName;
    }

    public void setEpicName(String epicName) {
        this.epicName = epicName;
    }

    public List<TestCaseStepDTO> getTestCaseStepDTOS() {
        return testCaseStepDTOS;
    }

    public void setTestCaseStepDTOS(List<TestCaseStepDTO> testCaseStepDTOS) {
        this.testCaseStepDTOS = testCaseStepDTOS;
    }

    @Override
    public String toString() {
        return StringUtil.getToString(this);
    }

}
