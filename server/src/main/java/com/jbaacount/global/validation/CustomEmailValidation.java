package com.jbaacount.global.validation;

import com.jbaacount.service.MemberService;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomEmailValidator.class)
@Documented
public @interface CustomEmailValidation
{
    String message() default "이미 사용중인 이메일입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Slf4j
@RequiredArgsConstructor
class CustomEmailValidator implements ConstraintValidator<CustomEmailValidation, String>
{
    private final MemberService memberService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context)
    {
        return !memberService.verifyExistEmail(email);
    }


}