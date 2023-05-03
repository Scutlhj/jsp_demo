package pers.sakurasora.onlinestore.web.servlet;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import pers.sakurasora.onlinestore.constant.Constant;
import pers.sakurasora.onlinestore.entity.Administrator;
import pers.sakurasora.onlinestore.entity.User;
import pers.sakurasora.onlinestore.service.AdminService;
import pers.sakurasora.onlinestore.service.impl.AdminServiceImpl;
import pers.sakurasora.onlinestore.web.servlet.base.BaseServlet;

/**
 * 
 * 
 * @Description
 *				Servlet--管理员模块
 */
@WebServlet("/admin")
public class AdminServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	
	private AdminService adminService = new AdminServiceImpl();

	/**
	 * 管理员登录
	 * @param 	request
	 * @param 	response
	 * @return
	 * @throws 	ServletException
	 * @throws 	IOException
	 */
	public String login(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		/**
		 * 1. 获取账号和密码<br>
		 * 2. 调用service完成登录 返回值:Administrator<br>
		 * 3. 判断Administrator 根据结果生成提示:<br>
		 * 		Administrator为null:  用户名或密码不正确<br>
		 * 		Administrator不为null: 重定向/jsp/admin/home.jsp）
		 */
		try {
			String sAccount = request.getParameter("account");
			String sPassword = request.getParameter("password");
			Administrator admin = adminService.login(sAccount,sPassword);
			
			if(admin == null){
				request.setAttribute("error", "用户名或密码不正确");;
				return "/jsp/admin/index.jsp";
			}

			JSONObject jsonObj = new JSONObject();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			jsonObj.put("login_time", dtf.format(LocalDateTime.now()));
			jsonObj.put("login_ip", request.getRemoteAddr());
			jsonObj.put("account", admin.getAccount());
			String json_to_string = JSONObject.toJSONString(jsonObj);
			System.out.println(json_to_string);
			writeFile(json_to_string,"C:\\Users\\Administrator\\Desktop\\Adminlogin.json");

			request.getSession().setAttribute("admin", admin); // 保存管理员登录状态
			
			response.sendRedirect(request.getContextPath() + "/jsp/admin/home.jsp"); //重定向到 管理中心/jsp/admin/home.jsp
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("查找管理员失败");
		}
		
		return null;
	}
	
	/**
	 * 退出
	 */
	public String logout(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
//		request.getSession().invalidate(); // 清除session中的所有信息
		Administrator admin = (Administrator)request.getSession().getAttribute("admin");

		JSONObject jsonObj = new JSONObject();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		jsonObj.put("logout_time", dtf.format(LocalDateTime.now()));
		jsonObj.put("logout_ip", request.getRemoteAddr());
		jsonObj.put("account", admin.getAccount());
		String json_to_string = JSONObject.toJSONString(jsonObj);
		System.out.println(json_to_string);
		writeFile(json_to_string,"C:\\Users\\Administrator\\Desktop\\Adminlogout.json");
		request.getSession().removeAttribute("admin"); // 将admin从session中移除
		
		PrintWriter pw = response.getWriter();
		pw.write("<script>window.parent.location.href='" + request.getContextPath() + "/jsp/admin/index.jsp';</script>");
		pw.flush();
		
		return null;
	}

	public void writeFile(String json, String FilePath) {

		try {
			File file = new File(FilePath);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			FileWriter fileWritter = new FileWriter(file,true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(json);
			bufferWritter.newLine();
			bufferWritter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
