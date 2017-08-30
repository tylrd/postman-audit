import httpretty
from click.testing import CliRunner

from postman_audit.cli import run

from tests.test_setup import register_collections_meta, register_collections


def setup_function(function):
    httpretty.enable()
    register_collections_meta()
    register_collections()


def test_cli():
    runner = CliRunner()
    result = runner.invoke(run, ["--key=key", "--audit-value=hello", "--request-part=headers", "--delay=0"])

    assert result.exit_code == 0
