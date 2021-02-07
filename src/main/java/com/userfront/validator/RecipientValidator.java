package com.userfront.validator;

import com.userfront.domain.Recipient;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RecipientValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Recipient.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Recipient recipient = (Recipient) o;

        if(recipient.getName().isEmpty())
            errors.rejectValue("name", "recipient.name.empty");
        if(recipient.getEmail().isEmpty())
            errors.rejectValue("email", "recipient.email.empty");
        if(recipient.getPhone().isEmpty())
            errors.rejectValue("phone", "recipient.phone.empty");
        if(recipient.getAccountNumber().isEmpty())
            errors.rejectValue("accountNumber", "recipient.ac.empty");

    }
}
