package cz.cvut.zuul.oaas.generators;

/**
 *
 * @author Tomas Mano <tomasmano@gmail.com>
 */
public interface OAuth2ClientCredentialsGenerator {

    String generateClientId();

    String generateClientSecret();
}