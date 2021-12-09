package ca.bc.gov.educ.api.gradstudent.util;

import ca.bc.gov.educ.api.gradstudent.model.dto.BaseModel;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

/**
 * Utility class used to construct {@link ResponseEntity} for various HTTP methods.
 */
@Component
public class ResponseHelper {

	@Autowired
	protected ModelMapper modelMapper;

	public ResponseEntity<Void> FORBIDDEN() {
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}

	public <T> ResponseEntity<T> NOT_FOUND() {
		return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
	}

	public <T> ResponseEntity<T> NO_CONTENT() {
		return new ResponseEntity<T>(HttpStatus.NO_CONTENT);
	}

	//************   GET methods

	/**
	 * Get Response Entity using JPA Entity Source
	 * @param <T>
	 * @param optional - the JPA entity Source
	 * @param type - The API model type to map to.
	 * @return
	 */
	protected <T> ResponseEntity<T> GET(Optional<?> optional, Class<T> type) {
		if (optional.isPresent()) {
			T model = modelMapper.map(optional.get(), type);
			if (model instanceof BaseModel) {
				return ResponseEntity.
						ok().						
						body(model);
			} else {
				return new ResponseEntity<>(model, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

	/**
	 * Get Response Entity using a LIST of JPA Entity Sources
	 * @param <T>
	 * @return
	 */
	public <T> ResponseEntity<List<T>> GET(Collection<?> entitySet, Type T) {
		if (entitySet == null || entitySet.isEmpty()) {
			return new ResponseEntity<List<T>>(new LinkedList<T>(), HttpStatus.OK);
		} else {
			List<T> list = modelMapper.map(Arrays.asList(entitySet.toArray()), T);
			return new ResponseEntity<List<T>>(list, HttpStatus.OK);
		}
	}
	
	/**
	 * Get Response Entity using a LIST of REST Model objects. This
	 * should only be used when you need fine-grained control over the model
	 * mapping and the mapping gets performed within the API.
	 * @param <T>
	 * @return
	 */
	public <T> ResponseEntity<List<T>> GET(List<T> list) {
		return new ResponseEntity<List<T>>(list, HttpStatus.OK);
	}

	/**
	 * Get Response entity directly from API model value
	 * @param <T>
	 * @param value
	 * @return
	 */
	public <T> ResponseEntity<T> GET(T value) {
		if (value == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (value instanceof BaseModel) {
			return ResponseEntity.
					ok().
					body(value);
		} else {
			return new ResponseEntity<>(value, HttpStatus.OK);
		}
	}

	
	//************   CREATE methods	
	public <T> ResponseEntity<ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel<T>> CREATED(Object returnValue, Class<T> type) {
		return new ResponseEntity<>(ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel.SUCCESS(modelMapper.map(returnValue, type)), HttpStatus.OK);
	}
	
	protected <T> ResponseEntity<ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel<T>> CREATED(Object returnValue, List<String> warningMessages, Class<T> type) {
		
		return new ResponseEntity<>(ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel.WARNING(modelMapper.map(returnValue, type), warningMessages), HttpStatus.MULTI_STATUS);
	}

	public <T> ResponseEntity<ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel<T>> CREATED(T returnValue) {
		return new ResponseEntity<>(ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel.SUCCESS(returnValue), HttpStatus.OK);
	}	
	
	//************   Updated methods	
	public <T> ResponseEntity<ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel<T>> UPDATED(Object returnValue, Class<T> type) {
		if (returnValue == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel.SUCCESS(modelMapper.map(returnValue, type)), HttpStatus.OK);
	}

	public <T> ResponseEntity<ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel<T>> UPDATED(T returnValue) {
		if (returnValue == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel.SUCCESS(returnValue), HttpStatus.OK);
	}
	
	protected ResponseEntity<ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel<Void>> UPDATED() {
		return new ResponseEntity<>(ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel.SUCCESS(null), HttpStatus.OK);
	}



	//************   DELETE methods
	public ResponseEntity<Void> DELETE(int deleteCount) {
		if (deleteCount == 0) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	protected ResponseEntity<Void> DELETE() {
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	protected <T> ResponseEntity<ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel<T>> ACCEPTED(Object returnValue, Class<T> type) {
		if (returnValue == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel.SUCCESS(modelMapper.map(returnValue, type)), HttpStatus.ACCEPTED);
	}
	
	/**
	 * Add converters to be used in model mapping.
	 * @param converters
	 */
	protected void addConverters(Converter<?, ?>... converters) {
		Stream.of(converters).forEach(converter -> modelMapper.addConverter(converter));
	}

}
