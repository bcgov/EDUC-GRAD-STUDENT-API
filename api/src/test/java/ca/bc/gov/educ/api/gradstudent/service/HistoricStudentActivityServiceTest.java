package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.HistoricStudentActivity;
import ca.bc.gov.educ.api.gradstudent.model.entity.HistoricStudentActivityEntity;
import ca.bc.gov.educ.api.gradstudent.repository.HistoricStudentActivityRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HistoricStudentActivityServiceTest {

    @InjectMocks
    HistoricStudentActivityService historicStudentActivityService;

    @Mock
    HistoricStudentActivityRepository historicStudentActivityRepository;

    private UUID studentID;
    private HistoricStudentActivityEntity entity1;
    private HistoricStudentActivityEntity entity2;

    @Before
    public void setUp() {
        studentID = UUID.randomUUID();
        entity1 = createHistoricStudentActivityEntity();
        entity2 = createHistoricStudentActivityEntity();
    }

    @Test
    public void testGetHistoricStudentActivities_withActivities_returnsMappedDTOs() {
        // Given
        List<HistoricStudentActivityEntity> entities = Arrays.asList(entity1, entity2);
        
        when(historicStudentActivityRepository.findByGraduationStudentRecordID(studentID))
                .thenReturn(entities);

        // When
        List<HistoricStudentActivity> actual = historicStudentActivityService.getHistoricStudentActivities(studentID);

        // Then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getHistoricStudentActivityID()).isEqualTo(entity1.getHistoricStudentActivityID());
        assertThat(actual.get(1).getHistoricStudentActivityID()).isEqualTo(entity2.getHistoricStudentActivityID());
        verify(historicStudentActivityRepository).findByGraduationStudentRecordID(studentID);
    }

    @Test
    public void testGetHistoricStudentActivities_withNoActivities_returnsEmptyList() {
        // Given
        List<HistoricStudentActivityEntity> entities = Collections.emptyList();
        
        when(historicStudentActivityRepository.findByGraduationStudentRecordID(studentID))
                .thenReturn(entities);

        // When
        List<HistoricStudentActivity> actual = historicStudentActivityService.getHistoricStudentActivities(studentID);

        // Then
        assertThat(actual).isEmpty();
        verify(historicStudentActivityRepository).findByGraduationStudentRecordID(studentID);
    }

    @Test
    public void testGetHistoricStudentActivities_withSingleActivity_returnsSingleDTO() {
        // Given
        List<HistoricStudentActivityEntity> entities = Collections.singletonList(entity1);
        
        when(historicStudentActivityRepository.findByGraduationStudentRecordID(studentID))
                .thenReturn(entities);

        // When
        List<HistoricStudentActivity> actual = historicStudentActivityService.getHistoricStudentActivities(studentID);

        // Then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getHistoricStudentActivityID()).isEqualTo(entity1.getHistoricStudentActivityID());
        verify(historicStudentActivityRepository).findByGraduationStudentRecordID(studentID);
    }

    @Test
    public void testGetHistoricStudentActivities_callsRepositoryWithCorrectStudentID() {
        // Given
        UUID specificStudentID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        List<HistoricStudentActivityEntity> entities = Collections.emptyList();
        
        when(historicStudentActivityRepository.findByGraduationStudentRecordID(specificStudentID))
                .thenReturn(entities);

        // When
        historicStudentActivityService.getHistoricStudentActivities(specificStudentID);

        // Then
        verify(historicStudentActivityRepository).findByGraduationStudentRecordID(specificStudentID);
    }

    private HistoricStudentActivityEntity createHistoricStudentActivityEntity() {
        HistoricStudentActivityEntity entity = new HistoricStudentActivityEntity();
        entity.setHistoricStudentActivityID(UUID.randomUUID());
        entity.setGraduationStudentRecordID(UUID.randomUUID());
        entity.setDate(LocalDateTime.now());
        entity.setType("ADD");
        entity.setProgram("2023");
        entity.setUserID("USER123");
        entity.setBatch("001");
        entity.setSeqNo("0001");
        entity.setCreateUser("TEST_USER");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateUser("TEST_USER");
        entity.setUpdateDate(LocalDateTime.now());
        return entity;
    }

}
