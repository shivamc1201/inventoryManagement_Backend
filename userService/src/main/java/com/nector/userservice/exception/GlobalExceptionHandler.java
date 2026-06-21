package com.nector.userservice.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFormatException(InvalidFormatException ex) {
        Map<String, String> error = new HashMap<>();
        // Check if the invalid value was for RoleType enum
        if (ex.getTargetType() != null && "RoleType".equals(ex.getTargetType().getSimpleName())) {
            error.put("error", "Invalid roleType. Allowed values are: "
                    + "PLANT_OFFICER, SALES_OFFICER, ACCOUNT_OFFICER, REGIONAL_SALES_MGR, "
                    + "ACCOUNT_MGR, LOGISTICS_OFFICER, LOGISTICS_MGR, BUSINESS_DEV_MGR, "
                    + "HR_EXECUTIVE, PLANT_EXECUTIVE, SALES_EXECUTIVE, ACCOUNT_EXECUTIVE, "
                    + "STATE_SALES_MGR, NATIONAL_SALES_MGR, HR_MGR, PLANT_MGR, "
                    + "ZONAL_SALES_MGR, ADMIN, AREA_SALES_MGR");
        } else {
            error.put("error", "Invalid request format");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleItemNotFound(ItemNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientStock(InsufficientStockException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound(CartNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartItemNotFound(CartItemNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(FinishedProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFinishedProductNotFound(FinishedProductNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(RawProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRawProductNotFound(RawProductNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MachinePartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleMachinePartNotFound(MachinePartNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(jakarta.persistence.EntityNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid operation - request cannot be processed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(InvalidCartStatusException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCartStatus(InvalidCartStatusException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DealerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleDealerNotFound(DealerNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateDealerPhoneException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateDealerPhone(DuplicateDealerPhoneException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DuplicateLedgerReferenceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateLedgerReference(DuplicateLedgerReferenceException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TenantAccessViolationException.class)
    public ResponseEntity<Map<String, String>> handleTenantAccessViolation(TenantAccessViolationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(ReservedLedgerNameException.class)
    public ResponseEntity<Map<String, String>> handleReservedLedgerName(ReservedLedgerNameException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DuplicateLedgerNameException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateLedgerName(DuplicateLedgerNameException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidLedgerGroupException.class)
    public ResponseEntity<Map<String, String>> handleInvalidLedgerGroup(InvalidLedgerGroupException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(VoucherNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleVoucherNotFound(VoucherNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(LedgerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLedgerNotFound(LedgerNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(TransactionValidationException.class)
    public ResponseEntity<Map<String, String>> handleTransactionValidation(TransactionValidationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> error = new HashMap<>();
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && ife.getTargetType() != null) {
            String typeName = ife.getTargetType().getSimpleName();
            if ("RoleType".equals(typeName)) {
                error.put("error", "Invalid roleType. Allowed values are: "
                        + "PLANT_OFFICER, SALES_OFFICER, ACCOUNT_OFFICER, REGIONAL_SALES_MGR, "
                        + "ACCOUNT_MGR, LOGISTICS_OFFICER, LOGISTICS_MGR, BUSINESS_DEV_MGR, "
                        + "HR_EXECUTIVE, PLANT_EXECUTIVE, SALES_EXECUTIVE, ACCOUNT_EXECUTIVE, "
                        + "STATE_SALES_MGR, NATIONAL_SALES_MGR, HR_MGR, PLANT_MGR, "
                        + "ZONAL_SALES_MGR, ADMIN, AREA_SALES_MGR");
            } else {
                error.put("error", "Invalid value '" + ife.getValue() + "' for field of type " + typeName);
            }
        } else {
            error.put("error", "Invalid or malformed request body");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingRequestParam(MissingServletRequestParameterException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Required parameter '" + ex.getParameterName() + "' is missing");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "An unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}

