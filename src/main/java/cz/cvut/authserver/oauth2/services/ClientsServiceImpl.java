package cz.cvut.authserver.oauth2.services;

import cz.cvut.authserver.oauth2.api.models.ClientDTO;
import cz.cvut.authserver.oauth2.dao.ClientDAO;
import cz.cvut.authserver.oauth2.generators.IdentifierGenerator;
import cz.cvut.authserver.oauth2.generators.OAuth2ClientCredentialsGenerator;
import cz.cvut.authserver.oauth2.models.Client;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 *
 *
 * @author Tomas Mano <tomasmano@gmail.com>
 */
public class ClientsServiceImpl implements ClientsService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientsServiceImpl.class);
    private static final List<GrantedAuthority> DEFAULT_AUTHORITIES =  AuthorityUtils.createAuthorityList("ROLE_CLIENT");

    private ClientDAO clientDAO;
    private MapperFacade mapper;
    private IdentifierGenerator identifierGenerator;
    private OAuth2ClientCredentialsGenerator credentialsGenerator;

    
    //////////  Business methods  //////////

    @Override
    public ClientDTO findClientById(String clientId) throws NoSuchClientException, OAuth2Exception {
        Client client = clientDAO.findOne(clientId);

        if (client == null) {
            throw new NoSuchClientException(String.format("Client with id [%s] doesn't exists.", clientId));
        }
        return mapper.map(client, ClientDTO.class);
    }

    @Override
    public String createClient(ClientDTO clientDTO) throws ClientAlreadyExistsException {
        Client client = mapper.map(clientDTO, Client.class);

        String clientId;
        do {
            LOG.debug("Generating new clientId");
            clientId = identifierGenerator.generateArgBasedIdentifier(client.getProductName());
        } while (clientDAO.exists(clientId));

        String clientSecret = credentialsGenerator.generateClientSecret();
        
        client.setClientId(clientId);
        client.setClientSecret(clientSecret);

        if (isEmpty(client.getAuthorities())) {
            client.setAuthorities(DEFAULT_AUTHORITIES);
        } else {
            client.setAuthorities(client.getAuthorities());
        }

        LOG.info("Saving new client: [{}]", client);
        clientDAO.save(client);

        return clientId;
    }

    @Override
    public void updateClient(ClientDTO clientDTO) throws NoSuchClientException {
        LOG.info("Updating client: [{}]", clientDTO);

        assertClientExists(clientDTO.getClientId());
        clientDAO.save(mapper.map(clientDTO, Client.class));
    }

    @Override
    public void removeClient(String clientId) throws NoSuchClientException {
        LOG.info("Removing client: [{}]", clientId);
        assertClientExists(clientId);

        clientDAO.delete(clientId);
    }

    @Override
    public void resetClientSecret(String clientId) throws NoSuchClientException {
        LOG.info("Resetting secret for client: [{}]", clientId);

        String newSecret = credentialsGenerator.generateClientSecret();
        try {
            clientDAO.updateClientSecret(clientId, newSecret);

        } catch (EmptyResultDataAccessException ex) {
            throw new NoSuchClientException(ex.getMessage(), ex);
        }
    }

    private void assertClientExists(String clientId) {
        if (! clientDAO.exists(clientId)) {
            throw new NoSuchClientException("No such client with id = " + clientId);
        }
    }

    
    //////////  Getters / Setters  //////////

    public void setClientDAO(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
    }

    public void setCredentialsGenerator(OAuth2ClientCredentialsGenerator credentialsGenerator) {
        this.credentialsGenerator = credentialsGenerator;
    }

    public void setMapperFacade(MapperFacade mapper) {
        this.mapper = mapper;
    }
}
