package cz.cvut.zuul.oaas.services

import cz.cvut.zuul.oaas.api.models.ResourceDTO
import cz.cvut.zuul.oaas.api.resources.exceptions.NoSuchResourceException
import cz.cvut.zuul.oaas.dao.ResourceDAO
import cz.cvut.zuul.oaas.generators.StringEncoder
import cz.cvut.zuul.oaas.models.Resource
import cz.cvut.zuul.oaas.test.factories.ObjectFactory
import spock.lang.Specification

import static cz.cvut.zuul.oaas.test.Assertions.assertThat

/**
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@Mixin(ObjectFactory)
class ResourcesServiceTest extends Specification {

    def resourceDao = Mock(ResourceDAO)
    def idEncoder = Mock(StringEncoder)

    def service = new ResourcesServiceImpl(
            resourceDAO: resourceDao,
            identifierEncoder: idEncoder
    )

    def setup() {
        service.setupMapper()
    }


    def 'create resource'() {
        given:
            def resource = build(ResourceDTO).with {
                it.resourceId = 'irrelevant'; return it
            }
            def generatedId = 'foo-123'
        when:
            def returnedId = service.createResource(resource)
        then:
            1 * idEncoder.encode(resource.name) >> generatedId
            1 * resourceDao.exists(generatedId) >> false
            1 * resourceDao.save({ Resource it ->
                it.id == generatedId
            })
            returnedId == generatedId
    }

    def 'create resource and handle generation of already taken id'() {
        given:
            def resource = build(ResourceDTO)
            def generatedId = 'foo-123'
        when:
            service.createResource(resource)
        then: 'generate unique id at the third attempt'
            3 * idEncoder.encode(resource.name) >>> ['taken-id', 'still-bad', generatedId]
            3 * resourceDao.exists(_) >>> [true, true, false]
        then:
            1 * resourceDao.save({ Resource it ->
                it.id == generatedId
            })
    }


    def 'update non existing resource'() {
        when:
            service.updateResource(build(ResourceDTO))
        then:
            resourceDao.exists(_) >> false
            thrown(NoSuchResourceException)
    }


    def 'find non existing resource by id'() {
        when:
            service.findResourceById('foo-123')
        then:
            1 * resourceDao.findOne('foo-123') >> null
            thrown(NoSuchResourceException)
    }


    def 'delete non existing resource by id'() {
        when:
            service.deleteResourceById('non-existing')
        then:
            resourceDao.exists('non-existing') >> false
            thrown(NoSuchResourceException)
    }


    def 'map Resource to ResourceDTO'() {
        given:
            def resource = build(Resource)
        when:
            def dto = service.mapper.map(resource, ResourceDTO)
        then:
            assertMapping resource, dto
    }

    def 'map ResourceDTO to Resource'() {
        given:
            def dto = build(ResourceDTO)
        when:
            def resource = service.mapper.map(dto, Resource)
        then:
            assertMapping resource, dto
    }

    private void assertMapping(Resource entity, ResourceDTO dto) {
        assertThat( entity ).equalsTo( dto ).inAllPropertiesExcept( 'auth', 'resourceId', 'visibility' )
        assert entity.id == dto.resourceId
        assert entity.visibility.toString() == dto.visibility

        assertThat( entity.scopes ).equalsTo( dto.auth.scopes ).inAllProperties()
    }
}
