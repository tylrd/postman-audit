from postman_audit.postman_client import PostmanClient
from postman_audit.auditor import PostmanAuditor

import httpretty

from tests.test_setup import register_collections_meta, register_collections


def setup_function(function):
    httpretty.enable()
    register_collections_meta()
    register_collections()


def teardown_function(function):
    httpretty.disable()


def test_audit_initialize():
    client = PostmanClient("key")
    auditor = PostmanAuditor(client, delay=0)
    assert len(auditor.collections) is 3
