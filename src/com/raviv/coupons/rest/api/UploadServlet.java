package com.raviv.coupons.rest.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.raviv.coupons.beans.Company;
import com.raviv.coupons.beans.Coupon;
import com.raviv.coupons.beans.User;
import com.raviv.coupons.blo.CompanysBlo;
import com.raviv.coupons.blo.UsersBlo;
import com.raviv.coupons.dao.CouponsDao;
import com.raviv.coupons.exceptions.ExceptionHandler;
import com.raviv.coupons.rest.api.outputs.GetCompanyOutput;
import com.raviv.coupons.rest.api.outputs.ServiceOutput;
import com.raviv.coupons.utils.LoginSession;


@WebServlet("/UploadServlet")
@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
maxFileSize=1024*1024*10,      // 10MB
maxRequestSize=1024*1024*50)   // 50MB
public class UploadServlet extends HttpServlet 
{
	static final long serialVersionUID = 1L;

	/**
	 * Name of the directory where uploaded files will be saved, relative to
	 * the web application directory.
	 */
	private static final String SAVE_DIR = "C:/Users/raviv/.babun/cygwin/home/raviv/workspaces/coupons/CouponsPhase2/WebContent/images/companies/";

	/**
	 * handles file upload
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ServiceOutput serviceOutput = new ServiceOutput();	
		// Get the printwriter object from response to write the required json object to the output stream      
		//PrintWriter out = response.getWriter();
		try 
		{
			Integer loginUserId = LoginSession.getLoginUserId(request);

			UsersBlo usersBlo = new UsersBlo();
			User loggedUser = usersBlo.getUserById( loginUserId);

			Company company = null;	
			CompanysBlo companysBlo = new CompanysBlo();

			/**
			 *  Get company, ADMIN profile
			 */		
			company =  companysBlo.getCompany( loggedUser );

			String savePath = SAVE_DIR + File.separator + company.getCompanyId();

			// creates the save directory if it does not exists
			File fileSaveDir = new File(savePath);
			if (!fileSaveDir.exists()) {
				fileSaveDir.mkdir();
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
			Date date = new Date();
			String ts = (dateFormat.format(date));
			
			String fileName = null;
			for (Part part : request.getParts()) {
				fileName = extractFileName(part);
				// refines the fileName in case it is an absolute path
				fileName = new File(fileName).getName();
				
				String fileExtention;
				int i = fileName.lastIndexOf('.');
				if (i > 0) {
					fileExtention = fileName.substring(i+1);
					fileName = ts + "." + fileExtention;
				}
				else{
					fileName = ts;
				}
				
				
				part.write(savePath + File.separator + fileName);
			}

			// ===================================================
			// Get the coupon for updating image file name
			// ===================================================
			CouponsDao couponDao = new CouponsDao();			
			Coupon coupon = couponDao.getCouponByCompanyIdForImageUpload (company.getCompanyId());

			// ===================================================
			// Update the coupon with image file name
			// ===================================================
			String imgPath = "WebContent/images/companies/" + company.getCompanyId() + "/"+ fileName;			
			coupon.setImageFileName(imgPath);
			couponDao.updateCoupon(coupon);


			//request.setAttribute("message", "Upload has been done successfully!");
			//response.setContentType("application/json");


			// Assuming your json object is **jsonObject**, perform the following, it will return your json object

			String jsonObject = "{ serviceStatus: { errorCode: \"0\", errorMessage : \"\",  success : \"true\" } }";
			//out.print(jsonObject);
			//out.flush();
		}
		catch (Throwable t) 
		{
			t.printStackTrace();
			serviceOutput.setServiceStatus(ExceptionHandler.createServiceStatus(t));
			String jsonObject = "{ serviceStatus: { errorCode: \"0\", errorMessage : \"\",  success : \"false\" } }";
			//out.print(jsonObject);
			//out.flush();
			//return;
		}		


		// return succsess
		//response.setContentType("application/json");
		//String jsonObject = "{ serviceStatus: { errorCode: \"0\", errorMessage : \"UploadServlet\",  success : \"true\" } }";
		//out.print(jsonObject);
		//out.flush();


		//getServletContext().getRequestDispatcher("/message.jsp").forward(
		//        request, response);


		response.setContentType("application/json");
		response.setHeader("Cache-Control", "nocache");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();

		JSONObject json = new JSONObject();

		// put some value pairs into the JSON object as into a Map.
		try 
		{
			json.put("status", 200);
			json.put("msg", "OK");

			// put a "map" 
			JSONObject map = new JSONObject();
			map.put("key1", "val1");
			map.put("key2", "val2");
			json.put("map", map);

			// put an "array"
			JSONArray arr = new JSONArray();
			arr.put(5);
			arr.put(3);
			arr.put(1);
			json.put("arr", arr);
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// finally output the json string		
		out.print(json.toString());

	}



	/**
	 * Extracts file name from HTTP header content-disposition
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length()-1);
			}
		}
		return "";
	}

}