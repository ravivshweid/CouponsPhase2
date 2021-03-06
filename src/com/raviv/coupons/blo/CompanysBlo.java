package com.raviv.coupons.blo;

import java.util.List;
import com.raviv.coupons.beans.Company;
import com.raviv.coupons.beans.User;
import com.raviv.coupons.dao.CompanysDao;
import com.raviv.coupons.dao.UsersDao;
import com.raviv.coupons.dao.interfaces.ICompanysDao;
import com.raviv.coupons.dao.interfaces.IUsersDao;
import com.raviv.coupons.dao.utils.JdbcTransactionManager;
import com.raviv.coupons.enums.ErrorType;
import com.raviv.coupons.enums.UserProfileType;
import com.raviv.coupons.exceptions.ApplicationException;
import com.raviv.coupons.utils.EmailValidator;
import com.raviv.coupons.utils.PrintUtils;

/**
 * 
 * Companys business logic
 * 
 * @author raviv
 *
 */
public class CompanysBlo  {

	
	private	CompanysDao				companysDao;
	
	public CompanysBlo() throws ApplicationException
	{
		this.companysDao 				= new CompanysDao();
	}
	
	public  void 			createCompany( User loggedUser, User newUser, Company company) throws ApplicationException 
	{
		// =====================================================
		// Verify admin profile id
		// =====================================================				
		ProfileIdVerifier.verifyAdminProfileId(loggedUser);

		// =====================================================
		// Verify valid email
		// =====================================================
		EmailValidator emailValidator =  new EmailValidator();
		if (  !emailValidator.isValid(company.getCompanyEmail())  )
		{
			throw new ApplicationException(ErrorType.INVALID_EMAIL,  "Company emmail is not valid : " + company.getCompanyEmail() );
		}

		// =====================================================
		// Verify user login name is not taken
		// =====================================================
		UsersDao 		userDao 		= new UsersDao();		
		if ( userDao.getUserByLoginName(newUser.getLoginName()) != null )
		{
			throw new ApplicationException(ErrorType.USER_LOGIN_NAME_ALREADY_EXISTS,  "USER LOGIN NAME ALREADY EXISTS : " + newUser.getLoginName() );
		}


		// =====================================================
		// Start transaction by creating JdbcTransactionManager
		// =====================================================		
		JdbcTransactionManager jdbcTransactionManager = new JdbcTransactionManager();
		// Inject transaction manager to DAO via constructors
		IUsersDao 		usersDao 	= new UsersDao   ( jdbcTransactionManager );
		ICompanysDao	companysDao	= new CompanysDao( jdbcTransactionManager );

		// =====================================================
		// Verify company name is not taken
		// =====================================================
		if ( companysDao.isCompanyNameExists( company.getCompanyName() ) )
		{
			throw new ApplicationException(ErrorType.COMPANY_NAME_ALREADY_EXISTS,  "COMPANY NAME ALREADY EXISTS : " + company.getCompanyName() );
		}
		
		try
		{
			// =====================================================
			// Create new user for the company
			// =====================================================
			newUser.setCreatedByUserId( loggedUser.getUserId() );
			newUser.setUpdatedByUserId( loggedUser.getUserId() );
			UserProfileType companyUserProfileType = UserProfileType.COMPANY;
			newUser.setUserProfileId( companyUserProfileType.getUserProfileId() );			
			// Create user in data layer
			usersDao.createUser(newUser);

			// =====================================================
			// Create new company with the new user
			// =====================================================
			int newUserId = newUser.getUserId();
			company.setUserId(newUserId);
			company.setCreatedByUserId( loggedUser.getUserId() );
			company.setUpdatedByUserId( loggedUser.getUserId() );
			// Create company in data layer
			companysDao.createCompany(company);
			
			// =====================================================
			// Commit transaction
			// =====================================================
			jdbcTransactionManager.commit();

			PrintUtils.printHeader("CompanysBlo : createCompany ended success");
			System.out.println(newUser);
			System.out.println(company);
			
		}
		catch (ApplicationException e)
		{
			// =====================================================
			// Rollback transaction
			// =====================================================
			jdbcTransactionManager.rollback();
			
			throw (e); 
		}
		finally
		{
			jdbcTransactionManager.closeConnection();
		}	
	}

	public  void 			deleteCompany( User loggedUser, long companyId) throws ApplicationException 
	{
		// =====================================================
		// Verify admin profile id
		// =====================================================				
		ProfileIdVerifier.verifyAdminProfileId(loggedUser);

		// =====================================================
		// Start transaction by creating JdbcTransactionManager
		// =====================================================		
		JdbcTransactionManager jdbcTransactionManager = new JdbcTransactionManager();
		// Inject transaction manager to DAO via constructor
		ICompanysDao companysDao	= new CompanysDao( jdbcTransactionManager );
		IUsersDao    usersDao	    = new UsersDao( jdbcTransactionManager );
		
		try
		{
			// =====================================================
			// Delete company related user
			// =====================================================
			Company company = companysDao.getCompany(companyId);
			if (company == null){return;}
			long userId = company.getUserId();
			usersDao.deleteUser( userId );
			PrintUtils.printHeader("CompanysBlo : deleteCompany deleted userId : " + userId );			
			
			
			// =====================================================
			// Delete company, related coupons and related customer coupons
			// COUPONS         has FK to COMPANYS using company id, with delete Cascade
			// CUSTOMER_COUPON has FK to COUPONS  using company id, with delete Cascade
			// =====================================================			
			companysDao.deleteCompany(companyId);
			
			// =====================================================
			// Commit transaction
			// =====================================================
			jdbcTransactionManager.commit();
			PrintUtils.printHeader("CompanysBlo : deleteCompany deleted companyId : " + companyId );			
		}
		catch (ApplicationException e)
		{
			// =====================================================
			// Rollback transaction
			// =====================================================
			jdbcTransactionManager.rollback();
			throw (e); 
		}
		finally
		{
			jdbcTransactionManager.closeConnection();
		}	
				
	}

	public  void 			updateCompany( User loggedUser, Company inputCompany) throws ApplicationException 
	{
		// =====================================================
		// Verify admin profile id
		// =====================================================				
		ProfileIdVerifier.verifyAdminProfileId(loggedUser);
		
		// =====================================================
		// Verify valid email
		// =====================================================
		EmailValidator emailValidator =  new EmailValidator();
		if (  !emailValidator.isValid(inputCompany.getCompanyEmail())  )
		{
			throw new ApplicationException(ErrorType.INVALID_EMAIL,  "Company emmail is not valid : " + inputCompany.getCompanyEmail() );
		}

		// =====================================================
		// Start transaction by creating JdbcTransactionManager
		// =====================================================		
		JdbcTransactionManager jdbcTransactionManager = new JdbcTransactionManager();
		// Inject transaction manager to DAO via constructors
		ICompanysDao companysDao	= new CompanysDao( jdbcTransactionManager );

		// =====================================================
		// Verify valid email
		// =====================================================
		if ( companysDao.isDuplicateCompanyNameExists (inputCompany.getCompanyId(), inputCompany.getCompanyName()) )
		{
			throw new ApplicationException(ErrorType.COMPANY_NAME_ALREADY_EXISTS,  "Duplicate company name : " + inputCompany.getCompanyEmail() );			
		}
		
		// =====================================================
		// Get the company from data layer
		// =====================================================				
		Company company;
		company = companysDao.getCompany( inputCompany.getCompanyId() );
		
		try
		{			
			// =====================================================
			// Update company
			// =====================================================
			// Prepare inputs
			company.setUpdatedByUserId	( loggedUser.getUserId() );
			company.setCompanyEmail		( inputCompany.getCompanyEmail() );
			company.setCompanyName		( inputCompany.getCompanyName() );
			// Update in data layer
			companysDao.updateCompany(company);

			// =====================================================
			// Commit transaction
			// =====================================================
			jdbcTransactionManager.commit();
			
			PrintUtils.printHeader("CompanysBlo : updateCompany updated company");
			System.out.println(company);
			
		}
		catch (ApplicationException e)
		{
			// =====================================================
			// Rollback transaction
			// =====================================================
			jdbcTransactionManager.rollback();
			throw (e); 
		}
		finally
		{
			jdbcTransactionManager.closeConnection();
		}	
	}

	public  List<Company>	getAllCompanys( User loggedUser) throws ApplicationException 
	{	
		// =====================================================
		// Verify admin profile id
		// =====================================================				
		ProfileIdVerifier.verifyAdminProfileId(loggedUser);

		// =====================================================
		// Get all company's from data layer
		// =====================================================				
		List<Company> companysList;
		companysList = companysDao.getAllCompanys();
		PrintUtils.printHeader("CompanysBlo : getAllCompanys");
		for ( Company company : companysList )
		{
			System.out.println(company);
		}
		return companysList;
	}
	
	public  Company 		getCompany( User loggedUser, long companyId) throws ApplicationException 
	{
		// =====================================================
		// Verify admin profile id
		// =====================================================				
		ProfileIdVerifier.verifyAdminProfileId(loggedUser);
	
		//==============================================
		// Get company from data layer by company id
		//==============================================		
		Company company = companysDao.getCompany(companyId);
		if ( company == null )
		{
			throw new ApplicationException(ErrorType.BLO_GET_ERROR , null, "Company not found, companyId : " + companyId );			
		}
		PrintUtils.printHeader("CompanysBlo : getCompany : companyId : " + companyId);		
		System.out.println(company);
		return company;
	}

	public  Company 		getCompany( User loggedUser) throws ApplicationException 
	{
		// =====================================================
		// Verify company profile id
		// =====================================================				
		ProfileIdVerifier.verifyCompanyProfileId(loggedUser);

		//==============================================
		// Get company from data layer by user id
		//==============================================		
		int userId      = loggedUser.getUserId();
		Company company = companysDao.getCompanyByUserId(userId);
		if ( company == null )
		{
			throw new ApplicationException(ErrorType.BLO_GET_ERROR, "Failed to get company with userId : " + userId );
		}
		PrintUtils.printHeader("CompanysBlo : getCompany : userId : " + userId);		
		System.out.println(company);		
		return company;
	}

}
