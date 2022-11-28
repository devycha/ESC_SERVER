package com.minwonhaeso.esc.common.exception.stadium;

import com.minwonhaeso.esc.common.exception.CustomException;
import com.minwonhaeso.esc.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class StadiumNotFoundException extends CustomException {
    public StadiumNotFoundException(ErrorCode errorcode) {
        super(errorcode);
    }
}
