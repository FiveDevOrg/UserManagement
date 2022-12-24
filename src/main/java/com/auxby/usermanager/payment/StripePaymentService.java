package com.auxby.usermanager.payment;

import com.auxby.usermanager.payment.model.PaymentRequest;
import com.auxby.usermanager.payment.model.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class StripePaymentService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void setup() {
        Stripe.apiKey = secretKey;
    }

    public PaymentResponse createPaymentIntent(PaymentRequest paymentRequest) throws StripeException {
        PaymentIntentCreateParams paymentIntentParams = new PaymentIntentCreateParams.Builder()
                .setAmount(computePaymentAmount(paymentRequest))
                .addPaymentMethodType(paymentRequest.paymentType())
                .setCurrency(paymentRequest.currency())
                .build();
        PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParams);

        return new PaymentResponse(paymentIntent.getClientSecret());
    }

    private long computePaymentAmount(PaymentRequest paymentRequest) {
        return (long) (paymentRequest.amount() * 100);
    }
}
