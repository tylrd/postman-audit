from postman_audit.audit import Auditor
from postman_audit.postman_client import PostmanClient

import httpretty
import json
import os


def setup_function(function):
    httpretty.enable()
    register_collections_meta()
    register_collections()


def register_collections_meta():
    collections_file = os.path.join(os.path.dirname(__file__), "data/collections.json")
    with open(collections_file) as f:
        collections_data = f.read()
    httpretty.register_uri(httpretty.GET, PostmanClient.BASE_URL + "/collections", body=collections_data, status=201,
                           content_type='application/json')


def register_collections():
    path = os.path.join(os.path.dirname(__file__), "data/collections")
    files = [os.path.join(path, f) for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
    for collection_file in files:
        with open(collection_file) as f:
            raw = f.read()
            data = json.loads(raw)
            httpretty.register_uri(httpretty.GET,
                                   PostmanClient.BASE_URL + "/collections/631643-%s" % data['collection']['info']['_postman_id'],
                                   body=raw, status=201, content_type='application/json')


def teardown_function(function):
    httpretty.disable()


def test_audit_initialize():
    client = PostmanClient("key")
    auditor = Auditor(client, delay=0)
    assert len(auditor.collections) is 3


def test_audit_headers():
    client = PostmanClient("key")
    auditor = Auditor(client, delay=0)
    auditor.audit_headers()
    assert True
