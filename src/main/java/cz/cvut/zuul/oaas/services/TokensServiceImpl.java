package cz.cvut.zuul.oaas.services;

import cz.cvut.oauth.provider.spring.TokenInfo;
import cz.cvut.zuul.oaas.api.models.TokenDTO;
import cz.cvut.zuul.oaas.api.resources.exceptions.NoSuchTokenException;
import cz.cvut.zuul.oaas.dao.AccessTokenDAO;
import cz.cvut.zuul.oaas.dao.ClientDAO;
import cz.cvut.zuul.oaas.models.Client;
import cz.cvut.zuul.oaas.models.PersistableAccessToken;
import cz.cvut.zuul.oaas.models.User;
import lombok.Getter;
import lombok.Setter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory.Builder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@Setter
@Service
public class TokensServiceImpl implements TokensService {

    private AccessTokenDAO accessTokenDAO;

    private ClientDAO clientDAO;

    /**
     * Orika Mapper Factory to be configured and used for mapping between entity
     * and DTO objects. If no factory is provided, then new one will be created.
     *
     * @see {@link #setupMapper()}
     */
    private MapperFactory mapperFactory;

    @Setter(NONE) @Getter(PACKAGE)
    private MapperFacade mapper;



    public TokenDTO getToken(String tokenValue) {
        PersistableAccessToken accessToken = accessTokenDAO.findOne(tokenValue);
        if (accessToken == null) throw new NoSuchTokenException();

        Client client = clientDAO.findOne(accessToken.getAuthenticatedClientId());

        TokenDTO dto = mapper.map(accessToken, TokenDTO.class);
        dto.getClientAuthentication().setClientLocked(client.isLocked());
        dto.getClientAuthentication().setProductName(client.getProductName());

        return dto;
    }

    public TokenInfo getTokenInfo(String tokenValue) {
        PersistableAccessToken accessToken = accessTokenDAO.findOne(tokenValue);

        // first check if token is recognized and if it is not expired
        if (accessToken == null) {
            throw new InvalidTokenException("Token was not recognised");
        }
        if (accessToken.isExpired()) {
            throw new InvalidTokenException("Token has expired");
        }

        Client client = clientDAO.findOne(accessToken.getAuthenticatedClientId());
        if (client == null) {
            throw new InvalidTokenException("Client doesn't exist anymore");
        }
        if (client.isLocked()) {
            throw new InvalidTokenException("Client is locked");
        }

        AuthorizationRequest clientAuth = accessToken.getAuthentication().getAuthorizationRequest();
        Authentication userAuth = accessToken.getAuthentication().getUserAuthentication();

        TokenInfo o = new TokenInfo();
        o.setExpiresIn( accessToken.getExpiresIn() );
        o.setScope( accessToken.getScope() );
        o.setAudience( clientAuth.getResourceIds() );
        o.setClientId( clientAuth.getClientId() );
        o.setClientAuthorities( clientAuth.getAuthorities() );

        if (userAuth != null) {
            o.setUserAuthorities( userAuth.getAuthorities() );
            o.setUserId( userAuth.getName() );

            if (userAuth.getPrincipal() instanceof User) {
                User user = (User) userAuth.getPrincipal();
                o.setUserEmail( user.getEmail() );
            }
        }
        return o;
    }

    public void invalidateToken(String tokenValue) {
        if (! accessTokenDAO.exists(tokenValue)) {
            throw new NoSuchTokenException();
        }
        accessTokenDAO.delete(tokenValue);
    }


    @PostConstruct void setupMapper() {
        MapperFactory factory = defaultIfNull(mapperFactory, new Builder().build());

        factory.registerClassMap(factory
                .classMap(PersistableAccessToken.class, TokenDTO.class)
                .field("value", "tokenValue")
                .field("authentication.authorizationRequest", "clientAuthentication")
                .field("authentication.userAuthentication.principal", "userAuthentication")
                .byDefault()
        );
        factory.registerClassMap(factory
                .classMap(AuthorizationRequest.class, TokenDTO.ClientAuthentication.class)
                .byDefault()
        );
        factory.registerClassMap(factory
                .classMap(User.class, TokenDTO.UserAuthentication.class)
                .byDefault()
        );
        mapper = factory.getMapperFacade();
    }
}
