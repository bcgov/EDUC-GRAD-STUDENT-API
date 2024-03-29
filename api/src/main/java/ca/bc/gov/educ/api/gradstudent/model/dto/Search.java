package ca.bc.gov.educ.api.gradstudent.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Search.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Search {

	/**
	   * The Condition.  ENUM to hold and AND OR
	   */
	  Condition condition;

	  /**
	   * The Search criteria list.
	   */
	  List<SearchCriteria> searchCriteriaList;
}
