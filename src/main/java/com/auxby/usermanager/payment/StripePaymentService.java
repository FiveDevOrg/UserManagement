package com.auxby.usermanager.payment;

import com.auxby.usermanager.payment.model.PaymentRequest;
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

    public String charge(PaymentRequest paymentRequest) throws StripeException {
        PaymentIntentCreateParams paymentIntentParams = new PaymentIntentCreateParams.Builder()
                .setAmount((long) (paymentRequest.amount() * 100))
                .setCurrency(paymentRequest.currency().name())
                .setDescription("Charge for bundle.")
                .setPaymentMethod(paymentRequest.paymentMethod())
                .build();
        PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParams);

        return paymentIntent.getInvoice();
    }
}
