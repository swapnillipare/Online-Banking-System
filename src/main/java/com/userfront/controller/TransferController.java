package com.userfront.controller;

import com.userfront.common.AccountType;
import com.userfront.common.TransferError;
import com.userfront.domain.PrimaryAccount;
import com.userfront.domain.Recipient;
import com.userfront.domain.SavingsAccount;
import com.userfront.domain.User;
import com.userfront.service.TransactionService;
import com.userfront.service.UserService;
import com.userfront.validator.RecipientValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/transfer")
public class TransferController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecipientValidator recipientValidator;

    @RequestMapping(value = "/betweenAccounts", method = RequestMethod.GET)
    public String betweenAccounts(Model model) {
        model.addAttribute("transferFrom", "");
        model.addAttribute("transferTo", "");
        model.addAttribute("amount", "");

        return "betweenAccounts";
    }

    @RequestMapping(value = "/betweenAccounts", method = RequestMethod.POST)
    public String betweenAccountsPost(
            @ModelAttribute("transferFrom") String transferFrom,
            @ModelAttribute("transferTo") String transferTo,
            @ModelAttribute("amount") String amount,
            Principal principal,
            HttpServletRequest request,
            Model model
    ) throws Exception {
        User user = userService.findByUsername(principal.getName());

        // even it handle from UI still handling here
        if (transferFrom.equals(transferTo)) {
            return sendErrorResponse(request, TransferError.SAME_ACCOUNT_TRANSFER.getErrorMessage(), "betweenAccounts");
        }

        BigDecimal transactionAmount = null;
        try {
            transactionAmount = new BigDecimal(amount);
        } catch (Exception e) {
            e.printStackTrace();
            return sendErrorResponse(request, TransferError.INVALID_TRANSFER_AMOUNT.getErrorMessage(), "betweenAccounts");
        }

        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();

        if (transferFrom.equals(AccountType.PRIMARY_ACCOUNT.getAccountType())) {
            if (primaryAccount.getAccountBalance().compareTo(transactionAmount) <= 0) {
                return sendErrorResponse(request, TransferError.LOW_BALANCE.getErrorMessage(), "betweenAccounts");
            }
        } else if (transferFrom.equals(AccountType.SAVING_ACCOUNT.getAccountType())) {
            if (savingsAccount.getAccountBalance().compareTo(transactionAmount) <= 0) {
                return sendErrorResponse(request, TransferError.LOW_BALANCE.getErrorMessage(), "betweenAccounts");
            }
        }

        if (transferFrom.equals(AccountType.SAVING_ACCOUNT.getAccountType())) {
            if (savingsAccount.getAccountBalance().compareTo(transactionAmount) <= 0) {
                model.addAttribute("transferFrom", transferFrom);
                model.addAttribute("transferTo", transferTo);
                model.addAttribute("amount", amount);
                return sendErrorResponse(request, TransferError.LOW_BALANCE.getErrorMessage(), "betweenAccounts");
            }
        }

        transactionService.betweenAccountsTransfer(transferFrom, transferTo, transactionAmount, primaryAccount, savingsAccount);

        return "redirect:/userFront";
    }

    @RequestMapping(value = "/recipient", method = RequestMethod.GET)
    public String recipient(Model model, Principal principal) {
        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        Recipient recipient = new Recipient();

        model.addAttribute("recipientList", recipientList);
        model.addAttribute("recipient", recipient);

        return "recipient";
    }

    @RequestMapping(value = "/recipient/save", method = RequestMethod.POST)
    public String recipientPost(@ModelAttribute("recipient") Recipient recipient,
                                Principal principal,
                                BindingResult bindingResult) {

        recipientValidator.validate(recipient, bindingResult);
        if (bindingResult.hasErrors()) {
            return "recipient";
        }

        User user = userService.findByUsername(principal.getName());
        recipient.setUser(user);
        transactionService.saveRecipient(recipient);

        return "redirect:/transfer/recipient";
    }

    @RequestMapping(value = "/recipient/edit", method = RequestMethod.GET)
    public String recipientEdit(@RequestParam(value = "recipientName") String recipientName, Model model, Principal principal) {

        Recipient recipient = transactionService.findRecipientByName(recipientName);
        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        model.addAttribute("recipientList", recipientList);
        model.addAttribute("recipient", recipient);

        return "recipient";
    }

    @RequestMapping(value = "/recipient/delete", method = RequestMethod.GET)
    @Transactional
    public String recipientDelete(@RequestParam(value = "recipientName") String recipientName, Model model, Principal principal) {

        transactionService.deleteRecipientByName(recipientName);

        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        Recipient recipient = new Recipient();
        model.addAttribute("recipient", recipient);
        model.addAttribute("recipientList", recipientList);


        return "recipient";
    }

    @RequestMapping(value = "/toSomeoneElse", method = RequestMethod.GET)
    public String toSomeoneElse(Model model, Principal principal) {
        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        model.addAttribute("recipientList", recipientList);
        model.addAttribute("accountType", "");

        return "toSomeoneElse";
    }

    @RequestMapping(value = "/toSomeoneElse", method = RequestMethod.POST)
    public String toSomeoneElsePost(@ModelAttribute("recipientName") String recipientName,
                                    @ModelAttribute("accountType") String accountType,
                                    @ModelAttribute("amount") String amount,
                                    Principal principal,
                                    HttpServletRequest request,
                                    Model model) {

        BigDecimal transactionAmount = null;
        try {
            transactionAmount = new BigDecimal(amount);
        } catch (Exception e) {
            e.printStackTrace();
            List<Recipient> recipientList = transactionService.findRecipientList(principal);
            model.addAttribute("recipientList", recipientList);
            model.addAttribute("accountType", "");
            return sendErrorResponse(request, TransferError.INVALID_TRANSFER_AMOUNT.getErrorMessage(), "toSomeoneElse");
        }

        if (recipientName.isEmpty() || accountType.isEmpty() || amount.isEmpty()) {
            List<Recipient> recipientList = transactionService.findRecipientList(principal);
            model.addAttribute("recipientList", recipientList);
            model.addAttribute("accountType", "");
            return this.sendErrorResponse(request, TransferError.INVALID_TRANSFER.getErrorMessage(), "toSomeoneElse");
        }

        User user = userService.findByUsername(principal.getName());
        Recipient recipient = transactionService.findRecipientByName(recipientName);
        if (recipient == null) {
            List<Recipient> recipientList = transactionService.findRecipientList(principal);
            model.addAttribute("recipientList", recipientList);
            model.addAttribute("accountType", "");
            return this.sendErrorResponse(request, TransferError.INVALID_RECIPIENT.getErrorMessage(), "toSomeoneElse");
        }

        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();
        if (accountType.equals(AccountType.PRIMARY_ACCOUNT.getAccountType())) {
            if (validateBalance(principal, model, transactionAmount, primaryAccount.getAccountBalance()))
                return sendErrorResponse(request, TransferError.LOW_BALANCE.getErrorMessage(), "toSomeoneElse");
        } else if (accountType.equals(AccountType.SAVING_ACCOUNT.getAccountType())) {
            if (validateBalance(principal, model, transactionAmount, savingsAccount.getAccountBalance()))
                return sendErrorResponse(request, TransferError.LOW_BALANCE.getErrorMessage(), "toSomeoneElse");
        }

        transactionService.toSomeoneElseTransfer(recipient, accountType, amount, primaryAccount, savingsAccount);

        return "redirect:/userFront";
    }

    private boolean validateBalance(Principal principal, Model model,
                                    BigDecimal transactionAmount, BigDecimal accountBalance) {
        if (accountBalance.compareTo(transactionAmount) <= 0) {
            List<Recipient> recipientList = transactionService.findRecipientList(principal);
            model.addAttribute("recipientList", recipientList);
            model.addAttribute("accountType", "");
            return true;
        }
        return false;
    }

    private String sendErrorResponse(HttpServletRequest request, String err, String redirectView) {
        request.setAttribute("err", err);
        return redirectView;
    }
}
