package com.auxby.usermanager.api.v1.payment;

import com.auxby.usermanager.api.v1.payment.model.ConfirmedPaymentRequest;
import com.auxby.usermanager.api.v1.payment.model.PaymentRequest;
import com.auxby.usermanager.api.v1.payment.model.PaymentResponse;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.entity.PaymentHistory;
import com.auxby.usermanager.entity.UserDetails;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.util.Date;

import static com.auxby.usermanager.utils.enums.PaymentStatusEnum.INTENT;
import static com.auxby.usermanager.utils.enums.PaymentStatusEnum.SUCCEEDED;

@Service
@RequiredArgsConstructor
public class StripePaymentService {
    @Value("${stripe.secret-key}")
    private String secretKey;

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final UserService userService;

    @PostConstruct
    public void setup() {
        Stripe.apiKey = secretKey;
    }

    public PaymentResponse createPaymentIntent(PaymentRequest paymentRequest, String userId) throws StripeException {
        UserDetails userDetails = userService.findUserDetails(userId);
        PaymentIntentCreateParams paymentIntentParams = new PaymentIntentCreateParams.Builder()
                .setAmount(computePaymentAmount(paymentRequest))
                .addPaymentMethodType(paymentRequest.paymentType())
                .setReceiptEmail(userDetails.getUserName())
                .setCurrency(paymentRequest.currency())
                .build();
        PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParams);
        savePayment(paymentIntent.getClientSecret(), userId);

        return new PaymentResponse(paymentIntent.getClientSecret());
    }

    @Transactional
    public void confirmedPayment(ConfirmedPaymentRequest confirmedPaymentRequest,
                                 String userUuid) {
        PaymentHistory payment = paymentHistoryRepository.findByPaymentSecretAndAccountUuidAndStatus(confirmedPaymentRequest.clientSecret(), userUuid, INTENT.name())
                .orElseThrow(() -> new EntityNotFoundException("Payment intent not found!"));
        payment.setStatus(SUCCEEDED.name());
        userService.addUserResources(confirmedPaymentRequest.coins(), userUuid);
    }

    private void savePayment(String clientSecret, String userUuid) {
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setPaymentSecret(clientSecret);
        paymentHistory.setIntentDate(new Date());
        paymentHistory.setAccountUuid(userUuid);
        paymentHistory.setStatus(INTENT.name());
        paymentHistoryRepository.save(paymentHistory);
    }

    private long computePaymentAmount(PaymentRequest paymentRequest) {
        return (long) (paymentRequest.amount() * 100);
    }
}
