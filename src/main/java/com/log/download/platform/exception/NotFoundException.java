package com.log.download.platform.exception;

import com.log.download.platform.response.ServerResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * NotFoundException
 * 404
 * @author: dongx
 * Description:
 * Created in: 2019-01-15 21:04
 * Modified by:
 */
@Controller
public class NotFoundException implements ErrorController {

	@Override
	public String getErrorPath() {
		return "/error";
	}

	@RequestMapping(value = {"/error"})
	@ResponseBody
	public ServerResponse error() {
		return ServerResponse.failure(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
	}

}
