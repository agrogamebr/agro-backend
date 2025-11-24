package br.com.agrogame.agrogame.exceptions;

public class AuthenticationException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String errorCode;
    
    public AuthenticationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
