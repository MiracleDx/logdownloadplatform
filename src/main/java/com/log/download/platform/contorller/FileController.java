package com.log.download.platform.contorller;

import com.alibaba.excel.EasyExcel;
import com.log.download.platform.bo.LogInfoBO;
import com.log.download.platform.entity.LogInfo;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.util.UploadListener;
import com.log.download.platform.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FileController
 * 上传下载控制器
 * @author Dongx
 * Description:
 * Created in: 2020-03-13 10:37
 * Modified by:
 */
@Slf4j
@Controller
@CrossOrigin
public class FileController {
	
	@PostMapping("upload")
	@ResponseBody
	public ServerResponse<LogInfo> upload(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			log.error("文件上传失败, 上传文件为空");
			return ServerResponse.failure("上传失败");
		}
		
		String fileName = "日志目录.xlsx";
		if (!StringUtils.equals(file.getOriginalFilename(), fileName)) {
			log.error("文件上传失败, 上传文件不正确, {}", file.getOriginalFilename());
			return ServerResponse.failure("上传失败, 请上传正确的文件");
		}
		
		UploadListener uploadListener = new UploadListener();
		EasyExcel.read(file.getInputStream(), LogInfo.class, uploadListener).sheet().doRead();
		// 转换BO
		List<LogInfoBO> bos = uploadListener.getList().stream().map(e -> {
			LogInfoBO bo = new LogInfoBO();
			BeanUtils.copyProperties(e, bo);
			return bo;
		}).collect(Collectors.toList());
		List<MenuVO> menu = convert2Tree(bos);
		return ServerResponse.success();
	}

	/**
	 * 查找所有一级菜单
	 * @param bos
	 * @return
	 */
	private List<MenuVO> convert2Tree(List<LogInfoBO> bos) {
		// 一级菜单树
		List<MenuVO> vos = new ArrayList<>();
		for (LogInfoBO bo : bos) {
			MenuVO vo = new MenuVO();
			vo.setLabel(bo.getNameSpace());
			vo.setChildren(new ArrayList<>());
			if (!vos.contains(vo)) {
				vos.add(vo);
			}
		}
		// 获取一级菜单
		Map<String, List<LogInfoBO>> firstMenu = bos.stream().collect(Collectors.groupingBy(LogInfoBO::getNameSpace));
		// 获取二级菜单
		Map<String, List<LogInfoBO>> secondMenu = bos.stream().collect(Collectors.groupingBy(LogInfoBO::getServerCode));
		
		// 二级菜单树
		for (MenuVO first : vos) {
			// 从一级菜单中获取对应的二级菜单
			List<LogInfoBO> seconds = firstMenu.get(first.getLabel());
			// 构造二级菜单
			for (LogInfoBO secondBO : seconds) {
				MenuVO second = new MenuVO();
				// 获取服务编码
				String serverCode = secondBO.getServerCode();
				second.setLabel(serverCode);
				// 从二级菜单中获取三级菜单
				List<LogInfoBO> thirdMenu = secondMenu.get(serverCode);
				List<MenuVO> thirds = new ArrayList<>();
				// 构造三级菜单
				for (LogInfoBO thirdBO : thirdMenu) {
					MenuVO third = new MenuVO();
					third.setLabel(thirdBO.getGroup());
					third.setChildren(new ArrayList<>());
					thirds.add(third);
				}
				second.setChildren(thirds); 
				
				// 一级菜单中不包含二级时添加
				if (!first.getChildren().contains(second)) {
					first.getChildren().add(second);
				}
			}
		}
		return vos;
	}
	
	
}
