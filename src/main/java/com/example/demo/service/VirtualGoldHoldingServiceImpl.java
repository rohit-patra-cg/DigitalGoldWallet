package com.example.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.SuccessResponse;
import com.example.demo.dto.VirtualGoldHoldingDTO;
import com.example.demo.entity.PhysicalGoldTransaction;
import com.example.demo.entity.TransactionHistory;
import com.example.demo.entity.User;
import com.example.demo.entity.VendorBranch;
import com.example.demo.entity.VirtualGoldHolding;
import com.example.demo.enums.TransactionStatus;
import com.example.demo.enums.TxnHistoryTransactionType;
import com.example.demo.exception.InvalidGoldQuantityException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.VendorBranchNotFoundException;
import com.example.demo.exception.VendorNotFoundException;
import com.example.demo.exception.VirtualGoldHoldingAreadyExistsException;
import com.example.demo.exception.VirtualGoldHoldingNotFoundException;
import com.example.demo.repository.PhysicalGoldTransactionRepository;
import com.example.demo.repository.TransactionHistoryRepository;
import com.example.demo.repository.VirtualGoldHoldingRepository;

import jakarta.transaction.Transactional;

@Service
public class VirtualGoldHoldingServiceImpl implements VirtualGoldHoldingService {
	
	private VirtualGoldHoldingRepository virtualGoldHoldingRepository;
	
	private UserService userService;

	private VendorService vendorService;
	
	private VendorBranchService branchService;
	
	private PhysicalGoldTransactionRepository physicalGoldTransactionRepository;
	
	private TransactionHistoryRepository transactionHistoryRepository;
	
	public VirtualGoldHoldingServiceImpl(VirtualGoldHoldingRepository virtualGoldHoldingRepository,
			UserService userService, VendorService vendorService, VendorBranchService branchService,
			PhysicalGoldTransactionRepository physicalGoldTransactionRepository,
			TransactionHistoryRepository transactionHistoryRepository) {
		this.virtualGoldHoldingRepository = virtualGoldHoldingRepository;
		this.userService = userService;
		this.vendorService = vendorService;
		this.branchService = branchService;
		this.physicalGoldTransactionRepository = physicalGoldTransactionRepository;
		this.transactionHistoryRepository = transactionHistoryRepository;
	}
	
	/**
	 * Get All Virtual Gold Holding
	 * @return List<VirtualGoldHolding> Collection of VirtualGoldHolding
	 */
	@Override
	public List<VirtualGoldHolding> getAllVirtualGoldHoldings() {
		return virtualGoldHoldingRepository.findAll();
	}
	
	/**
	 * Get Virtual Gold Holding by holding_id
	 * @param holdingId
	 * @return VirtualGoldHolding Object
	 * @throws virtualGoldHoldingNotFoundException
	 */
	@Override
	public VirtualGoldHolding getVirtualGoldHoldingById(int holdingId) throws VirtualGoldHoldingNotFoundException {
		return virtualGoldHoldingRepository.findById(holdingId).orElseThrow(() -> new VirtualGoldHoldingNotFoundException("VirtualGoldHolding#" + holdingId + " not found"));
	}
    
	/**
	 * Get All Virtual Gold Holding by users_id
	 * @param userId
	 * @return Collection of VirtualGoldHolding
	 * @throws UserNotFoundException
	 */
	@Override
	public List<VirtualGoldHolding> getAllVirtualGoldHoldingsByUserId(int userId) throws UserNotFoundException {
		userService.getUserByUserId(userId);
		return virtualGoldHoldingRepository.findAllVirtualGoldHoldingByUserId(userId);
	}
	
	/**
	 * Get Virtual Gold Holding for a specific user and vendor
	 * @param userId, vendorId
	 * @return Collection of VirtualGoldHolding 
	 * @throws UserNotFoundException
	 * @throws VendorNotFoundException
	 */
	@Override
	public List<VirtualGoldHolding> getAllVirtualGoldHoldingsByUserIdAndVendorId(int userId, int vendorId) throws UserNotFoundException, VendorNotFoundException {
		userService.getUserByUserId(userId);
		vendorService.getVendorById(vendorId);
		return virtualGoldHoldingRepository.findAllVirtualGoldHoldingByUserIdAndVendorId(userId, vendorId);
	}
	
	/**
	 * Add New Virtual Gold Holding
	 * @param  holdingDto 
	 * @return SuccessResponse Response for successfully adding new virtual gold holding
	 * @throws VirtualGoldHoldingAreadyExistsException
	 * @throws UserNotFoundException 
	 * @throws VendorBranchNotFoundException
     */
	@Override
	public SuccessResponse addVirtualGoldHolding(VirtualGoldHoldingDTO holdingDto)throws VirtualGoldHoldingAreadyExistsException, UserNotFoundException, VendorBranchNotFoundException {
		User user = userService.getUserByUserId(holdingDto.getUserId());
		VendorBranch branch=branchService.getVendorBranchByBranchId(holdingDto.getBranchId());
		VirtualGoldHolding virtualGoldHolding=new VirtualGoldHolding();
		virtualGoldHolding.setBranch(branch);
		virtualGoldHolding.setUser(user);
		virtualGoldHolding.setQuantity(holdingDto.getQuantity());
		VirtualGoldHolding savedVirtualGoldHolding = virtualGoldHoldingRepository.save(virtualGoldHolding);
		return new SuccessResponse(new Date(),"Virtual Gold Holding data added successfully", savedVirtualGoldHolding.getHoldingId());
	}
	
	/**
	 * Update Virtual Gold Holding by holding_id
	 * @Param holdingId, holdingDto
	 * @return SuccessResponse Response for successfully updating virtual gold holding
	 * @throws VirtualGoldHoldingNotFoundException 
	 * @throws UserNotFoundException
	 * @throws VendorBranchNotFoundException
	 */
    @Override
	public SuccessResponse updateVirtualGoldHolding(int holdingId, VirtualGoldHoldingDTO holdingDto)
			throws VirtualGoldHoldingNotFoundException,UserNotFoundException, VendorBranchNotFoundException {
		User user = userService.getUserByUserId(holdingDto.getUserId());
		VendorBranch branch=branchService.getVendorBranchByBranchId(holdingDto.getBranchId());
		VirtualGoldHolding virtualGoldHolding=getVirtualGoldHoldingById(holdingId);
		virtualGoldHolding.setBranch(branch);
		virtualGoldHolding.setUser(user);
		virtualGoldHolding.setQuantity(holdingDto.getQuantity());
		virtualGoldHoldingRepository.save(virtualGoldHolding);
		return new SuccessResponse(new Date(),"Virtual Gold Holding data updated successfully", holdingId);
	}
    
    /**
     * Convert Virtual Gold Holding to Physical gold holding 
     * @param quantity,  holdingId
     * @return SuccessResponse Response for successfully converting virtual gold holding to physical gold holding 
     * @throws VirtualGoldHoldingNotFoundException 
     * @throws UserNotFoundException 
     * @throws VendorBranchNotFoundException 
     * @throws InvalidGoldQuantityException
     */
	@Override
	@Transactional
	public SuccessResponse convertToPysical(double quantity, int holdingId)
			throws VirtualGoldHoldingNotFoundException, UserNotFoundException, VendorBranchNotFoundException, InvalidGoldQuantityException {
		VirtualGoldHolding virtualGoldHolding = getVirtualGoldHoldingById(holdingId);
		if (quantity <= 0) {
			throw new InvalidGoldQuantityException("Invalid gold quantity");
		}
		Double virtualGoldQuantity = virtualGoldHolding.getQuantity();
		if (virtualGoldQuantity < quantity) {
			throw new InvalidGoldQuantityException("Quantity must be less than " + virtualGoldQuantity);
		}
		virtualGoldHolding.setQuantity(virtualGoldQuantity - quantity);
		virtualGoldHoldingRepository.save(virtualGoldHolding);
		
		PhysicalGoldTransaction physicalGoldTransaction = new PhysicalGoldTransaction();
		physicalGoldTransaction.setUser(virtualGoldHolding.getUser());
		physicalGoldTransaction.setBranch(virtualGoldHolding.getBranch());
		physicalGoldTransaction.setDeliveryAddress(virtualGoldHolding.getUser().getAddress());
		physicalGoldTransaction.setQuantity(quantity);
		physicalGoldTransactionRepository.save(physicalGoldTransaction);
		
		TransactionHistory transactionHistory = new TransactionHistory();
		transactionHistory.setUser(virtualGoldHolding.getUser());
		transactionHistory.setTransactionType(TxnHistoryTransactionType.CONVERT_TO_PHYSICAL);
		transactionHistory.setTransactionStatus(TransactionStatus.SUCCESS);
		transactionHistory.setQuantity(quantity);
		transactionHistory.setAmount(quantity * virtualGoldHolding.getBranch().getVendor().getCurrentGoldPrice());
		transactionHistory.setBranch(virtualGoldHolding.getBranch());
		transactionHistoryRepository.save(transactionHistory);
		return new SuccessResponse(new Date(), "Virtual Gold data convert successfully", holdingId);
	}
	
}
