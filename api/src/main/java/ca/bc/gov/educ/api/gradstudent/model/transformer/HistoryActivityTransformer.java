package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.HistoryActivity;
import ca.bc.gov.educ.api.gradstudent.model.entity.HistoryActivityCodeEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class HistoryActivityTransformer {

    @Autowired
    ModelMapper modelMapper;

    public HistoryActivity transformToDTO (HistoryActivityCodeEntity historyActivityEntity) {
    	return modelMapper.map(historyActivityEntity, HistoryActivity.class);
    }

    public HistoryActivity transformToDTO ( Optional<HistoryActivityCodeEntity> historyActivityEntity ) {
    	HistoryActivityCodeEntity cae = new HistoryActivityCodeEntity();
        if (historyActivityEntity.isPresent())
            cae = historyActivityEntity.get();

        return modelMapper.map(cae, HistoryActivity.class);
    }

	public List<HistoryActivity> transformToDTO (Iterable<HistoryActivityCodeEntity> historyActivityEntities ) {
		List<HistoryActivity> historyActivityList = new ArrayList<>();
        for (HistoryActivityCodeEntity HistoryActivityEntity : historyActivityEntities) {
        	HistoryActivity historyActivity = modelMapper.map(HistoryActivityEntity, HistoryActivity.class);
        	historyActivityList.add(historyActivity);
        }
        return historyActivityList;
    }

    public HistoryActivityCodeEntity transformToEntity(HistoryActivity historyActivity) {
        return modelMapper.map(historyActivity, HistoryActivityCodeEntity.class);
    }
}
