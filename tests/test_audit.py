from unittest import TestCase
from postman_audit.audit import Auditor
from postman_audit.postman_client import PostmanClient


class AuditTest(TestCase):
    def test_audit_initialize(self):
        client = PostmanClient("key")
        auditor = Auditor(client)
        auditor.initialize()
        self.assertTrue(auditor.collections.count() is not 0)

    def test_audit_headers(self):
        client = PostmanClient("key")
        auditor = Auditor(client)
        auditor.initialize()
        auditor.audit_headers()
        self.assertTrue(True)

    def test_audit_prerequest(self):
        client = PostmanClient("key")
        auditor = Auditor(client)
        auditor.initialize()
        auditor.audit_prerequest()
        self.assertTrue(True)
