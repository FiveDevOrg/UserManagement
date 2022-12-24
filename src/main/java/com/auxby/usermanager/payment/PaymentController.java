package com.auxby.usermanager.payment;

import com.auxby.usermanager.payment.model.PaymentRequest;
import com.auxby.usermanager.payment.model.PaymentResponse;
import com.auxby.usermanager.utils.constant.AppConstant;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = AppConstant.BASE_V1_URL)
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/create-payment-intent")
    public PaymentResponse createPaymentIntent(@Valid @RequestBody PaymentRequest paymentRequest) throws StripeException {
        log.info("POST - trigger a payment.");
        return stripePaymentService.createPaymentIntent(paymentRequest);
    }
}
