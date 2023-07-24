package ca.bc.gov.educ.api.gradstudent.config;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateDeserializer;
import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateSerializer;
import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateTimeDeserializer;
import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@EntityScan(basePackages = {"ca.bc.gov.educ.api.gradstudent.model.entity"} )
@EnableJpaRepositories(basePackages = {"ca.bc.gov.educ.api.gradstudent.repository"})
public class EducGradStudentApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(GraduationStudentRecordEntity.class, GraduationStudentRecord.class).addMappings(mapper -> mapper.skip(GraduationStudentRecord::setProgramCompletionDate));
        modelMapper.typeMap(GraduationStudentRecord.class, GraduationStudentRecordEntity.class).addMappings(mapper -> mapper.skip(GraduationStudentRecordEntity::setProgramCompletionDate));
        return modelMapper;
    }

    /**
     * Lock provider lock provider.
     *
     * @param jdbcTemplate       the jdbc template
     * @param transactionManager the transaction manager
     * @return the lock provider
     */
    @Bean
    public LockProvider lockProvider(@Autowired JdbcTemplate jdbcTemplate, @Autowired PlatformTransactionManager transactionManager) {
        return new JdbcTemplateLockProvider(jdbcTemplate, transactionManager, "STATUS_SHEDLOCK");
    }

    @Bean
    @Primary
    ObjectMapper jacksonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDate.class, new GradLocalDateSerializer());
        simpleModule.addSerializer(LocalDateTime.class, new GradLocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDate.class, new GradLocalDateDeserializer());
        simpleModule.addDeserializer(LocalDateTime.class, new GradLocalDateTimeDeserializer());
        mapper.findAndRegisterModules();
        mapper.registerModule(simpleModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

}
