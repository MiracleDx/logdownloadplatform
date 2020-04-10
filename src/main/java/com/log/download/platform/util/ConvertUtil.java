package com.log.download.platform.util;

import com.log.download.platform.support.ParameterInvalidItem;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ConvertUtil
 *
 * @author Dongx
 * Description:
 * Created in: 2019-01-17 9:25
 * Modified by:
 */
public class ConvertUtil {
	
	public static List<ParameterInvalidItem> convertBindingResultToMapParameterInvalidItemList(BindingResult result) {
		List<ParameterInvalidItem> itemList = new ArrayList<>();
		List<FieldError> fieldErrors = result.getFieldErrors();
		ParameterInvalidItem parameterInvalidItem = null;
		for (FieldError fieldError : fieldErrors) {
			parameterInvalidItem = ParameterInvalidItem.builder()
					.filedName(fieldError.getField())
					.errorMessage(fieldError.getDefaultMessage())
					.build();
			itemList.add(parameterInvalidItem);
		}
		return itemList;
	}

	public static List<ParameterInvalidItem> convertCVSetToParameterInvalidItemList(Set<ConstraintViolation<?>> constraintViolations) {
		List<ParameterInvalidItem> itemList = new ArrayList<>();
		ParameterInvalidItem parameterInvalidItem = null;
		for (ConstraintViolation<?> constraintViolation : constraintViolations) {
			parameterInvalidItem = ParameterInvalidItem.builder()
					.filedName(constraintViolation.getInvalidValue().toString())
					.errorMessage(constraintViolation.getMessage())
					.build();
			itemList.add(parameterInvalidItem);
		}
		return itemList;
	}
	
}
