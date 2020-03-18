package com.log.download.platform.service;

import com.alibaba.excel.EasyExcel;
import com.log.download.platform.bo.DeploymentGroupBO;
import com.log.download.platform.entity.DeploymentGroup;
import com.log.download.platform.util.UploadListener;
import com.log.download.platform.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MenuService
 *
 * @author Dongx
 * Description:
 * Created in: 2020-03-18 9:55
 * Modified by:
 */
@Slf4j
@Service
public class MenuService {

	/**
	 * 菜单树
	 */
	public List<MenuVO> menu = new ArrayList<>();
	
	/**
	 * 获取菜单树
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public List<MenuVO> getMenu(InputStream in) throws IOException {
		UploadListener uploadListener = new UploadListener();
		EasyExcel.read(in, DeploymentGroup.class, uploadListener).sheet().doRead();
		// 转换BO
		List<DeploymentGroupBO> bos = uploadListener.getList().stream().map(e -> {
			DeploymentGroupBO bo = new DeploymentGroupBO();
			BeanUtils.copyProperties(e, bo);
			return bo;
		}).collect(Collectors.toList());
		//Stream.iterate(0, i -> i + 1).limit(bos.size()).forEach(i -> bos.get(i).setId(i));
		
		// 获取aop代理对象
		MenuService o = (MenuService) AopContext.currentProxy();
		o.menu = convert2Tree(bos);
		log.info("菜单树转换成功");
		in.close();
		return menu;
	}

	/**
	 * 查找所有一级菜单
	 * @param bos
	 * @return
	 */
	private List<MenuVO> convert2Tree(List<DeploymentGroupBO> bos) {
		// 一级菜单树
		List<MenuVO> vos = new ArrayList<>();
		for (DeploymentGroupBO bo : bos) {
			MenuVO vo = new MenuVO();
			vo.setLabel(bo.getNameSpace());
			vo.setChildren(new ArrayList<>());
			if (!vos.contains(vo)) {
				vos.add(vo);
			}
		}
		// 获取一级菜单
		Map<String, List<DeploymentGroupBO>> firstMenu = bos.stream().collect(Collectors.groupingBy(DeploymentGroupBO::getNameSpace));
		// 获取二级菜单
		Map<String, List<DeploymentGroupBO>> secondMenu = bos.stream().collect(Collectors.groupingBy(DeploymentGroupBO::getApplicationName));
		// 获取各部署组对应的IP清单
		Map<String, List<DeploymentGroupBO>> groupMap = bos.stream().collect(Collectors.groupingBy(DeploymentGroupBO::getGroup));

		// 二级菜单树
		for (MenuVO first : vos) {
			// 从一级菜单中获取对应的二级菜单
			List<DeploymentGroupBO> seconds = firstMenu.get(first.getLabel());
			// 增加一级菜单的中文名称
			first.setLabel(first.getLabel() + "-" + seconds.get(0).getProjectName());
			// 构造二级菜单
			for (DeploymentGroupBO secondBO : seconds) {
				MenuVO second = new MenuVO();
				// 获取服务编码
				String serverCode = secondBO.getApplicationName();
				second.setLabel(serverCode + "-" + secondBO.getServerName());
				// 从二级菜单中获取三级菜单
				List<DeploymentGroupBO> thirdMenu = secondMenu.get(serverCode);
				List<MenuVO> thirds = new ArrayList<>();
				// 构造三级菜单
				for (DeploymentGroupBO thirdBO : thirdMenu) {
					MenuVO third = new MenuVO();
					String group = thirdBO.getGroup();
					// 取中心名称
					String cluster = thirdBO.getCluster();
					String[] strs = cluster.split("-");
					if (strs != null && strs.length > 0) {
						third.setCluster(strs[0]);
					} else {
						third.setCluster(cluster.substring(0, 2));
					}
					third.setLabel(group);
					third.setChildren(new ArrayList<>());
					third.setIps(groupMap.get(group).stream().map(DeploymentGroupBO::getIp).filter(StringUtils::isNoneBlank).collect(Collectors.toList()));

					// 判断总/分公司
					String[] split = group.split("-");
					boolean flag;
					// 普通中心
					if (split.length == 4) {
						flag = first.getLabel().contains(split[2]);
						// ua 前台页面 前台应用
					} else {
						flag = true;
					}

					if (!thirds.contains(third) && flag) {
						thirds.add(third);
					}
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
