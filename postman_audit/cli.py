from postman_audit.auditor import PostmanAuditor

import logging
import click
import json
import sys

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@click.command()
@click.option("--key", "-k", envvar='POSTMAN_API_KEY', required=True,
              help="Postman Pro API key. If not provided, looks for env variable POSTMAN_API_KEY")
@click.option("--request-part", "-p", type=click.Choice(['headers', 'prerequest']), required=True,
              help="Type of audit.")
@click.option("--audit-value", "-a", 'audit_values', multiple=True,
              help="Value to look for during audit. Has precedence over --file")
@click.option("--file", "-f", default=None,
              help="Absolute path to JSON file that contains values. {\"audit_values\":[]}")
@click.option("--delay", "-d", type=int, default=5, help="Delay between making Postman API calls")
@click.option("--verbose", "-v", default=False)
def run(key, request_part, audit_values, delay, file, verbose):
    audit_values = list(audit_values)
    if file is not None:
        try:
            with open(file) as f:
                raw = f.read()
                data = json.loads(raw)
                audit_values += data['audit_values']
        except:
            logging.exception("Error loading configuration file %s", file)
            click.echo("Error loading configuration file.")

    if not len(audit_values):
        click.echo("Must provide audit values via --audit-value (-a) or --file (-f). ")
        sys.exit(1)

    auditor = PostmanAuditor(postman_key=key, delay=delay)

    click.echo("Successfully initialized postman auditor.")

    switcher = {
        'headers': audit_headers,
        'prerequest': audit_prerequest
    }

    func = switcher.get(request_part)
    auditor.audit(func(audit_values))


def audit_prerequest(vals):
    def func(item, name):
        if 'event' in item:
            for event in item['event']:
                if 'listen' in event and event['listen'] == 'prerequest' and 'script' in event:
                    if 'exec' in event['script']:
                        script = event['script']['exec']
                        for line in script:
                            if any(val in line for val in vals):
                                click.echo("FOUND! sensitive data in [ %s / %s ]" % (name, item['name']))

    return func


def audit_headers(vals):
    def func(item, name):
        if 'request' in item and 'header' in item['request']:
            for header in item['request']['header']:
                if any(val in header['value'] for val in vals):
                    click.echo("FOUND! sensitive data in [ %s / %s ]" % (name, item['name']))

    return func
