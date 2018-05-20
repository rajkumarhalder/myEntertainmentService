package com.my.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.my.dao.SequenceDao;
import com.my.model.LoginInfo;
import com.my.model.MemberDetails;
import com.my.model.MonthMaster;
import com.my.model.Payments;
import com.my.repository.MonthMasterRepository;
import com.my.repository.PaymentRepository;
import com.my.repository.RegistrationRepository;
import com.my.service.RegistrationService;
import com.my.util.EntertainmentConstant;


@Service
public class RegistrationServiceImpl implements RegistrationService{
	
	@Autowired
	RegistrationRepository registrationRepository;
	
	@Autowired
	PaymentRepository paymentRepository;
	
	@Autowired
	MonthMasterRepository monthMasterRepository;
	
	@Autowired
	SequenceDao sequenceDao;

	@Override
	public Object memberRegistration(Object object) {
		MemberDetails member=null;
		if(object instanceof MemberDetails)
			member=(MemberDetails) object;
		
		member.setMemberId(sequenceDao.getNextSequenceId("member_seq"));
		
		registrationRepository.save(member);
		insertPayment(member.getMemberId());
		
		return "success";
	}
	
	@Override
	public Object getAll() {
		
		return registrationRepository.findAll();
	}
	
	@Override
	public MemberDetails executeLogin(String userName,String passWord) {
		
		MemberDetails memberDetails=null;
		try {
			memberDetails=registrationRepository.findByUserNamePassword(userName, passWord);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return memberDetails;
	}
	
	@Override
	public MemberDetails getMemberByMemberId(Long memberId) {
		
		MemberDetails memberDetails=null;
		try {
			memberDetails=registrationRepository.findMemberByMemberId(memberId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return memberDetails;
	}
	
	@Override
	public List<Payments> getPaymentsByMemberId(Long memberId) {
		
		List<Payments> listPayments=null;
		try {
			listPayments=paymentRepository.getPaymentsByMember(memberId);
			
			for (Payments payments : listPayments) {
				if(payments.getMonthId()>new Date().getMonth() && EntertainmentConstant.PAYMENT_STATUS_NA.equalsIgnoreCase(payments.getPaymentStatus())) {
					payments.setPaymentStatus(EntertainmentConstant.PAYMENT_STATUS_PENDING);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listPayments;
	}
	
	
	@Override
	public void insertPayment(Long memberId) {
		
		try {
			List<MonthMaster> listMonth=monthMasterRepository.findAll();
			
			if(null!=listMonth && ! listMonth.isEmpty()) {
				
				for (MonthMaster monthMaster : listMonth) {
					Payments payments=new Payments();
					payments.setMonthId(monthMaster.getMonthId());
					payments.setAmount(EntertainmentConstant.PAYMENT_TARGET_AMOUNT);
					payments.setMemberId(memberId);
					payments.setMonth(monthMaster.getMonth());
					payments.setYear(EntertainmentConstant.CURRENT_YEAR);
					payments.setPaymentDate("--");
					payments.setPaymentStatus(EntertainmentConstant.PAYMENT_STATUS_NA);
					payments.setPaymentId(sequenceDao.getNextSequenceId("payments"));
					
					paymentRepository.save(payments);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public List<MonthMaster> getMonthMaster() {
		
		List<MonthMaster> listPayments=null;
		try {
			listPayments=monthMasterRepository.findAll();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listPayments;
	}
	
	
	@Override
	public void updatePayment(Payments payments,LoginInfo info) {
		
		try {
			
			List<Payments> list=paymentRepository.getPaymentsByMemberandMonth(payments.getMemberId(), payments.getMonthId());
		if(null !=list && !list.isEmpty()) {
			Payments existingPaymen=list.get(0);
			
			existingPaymen.setAmount(payments.getAmount());
			if(payments.getAmount().equals(EntertainmentConstant.PAYMENT_TARGET_AMOUNT))
				existingPaymen.setPaymentStatus(EntertainmentConstant.AUTHENTICATION_DONE);
			
			paymentRepository.save(existingPaymen);
		}
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

}
