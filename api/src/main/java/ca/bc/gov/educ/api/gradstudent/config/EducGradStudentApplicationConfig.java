package ca.bc.gov.educ.api.gradstudent.config;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.TimeZone;

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
    ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false)
                // Set timezone for JSON serialization as system timezone
                .timeZone(TimeZone.getDefault())
                .build();
    }

    @Bean
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder();
    }

}
