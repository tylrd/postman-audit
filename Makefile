.PHONY:

all: test

install-dev: venv
	pip install -q -e .[dev]

analysis:
	flake8 --ignore=E123,E126,E128,E501,W391,W291,W293,F401 tests
	flake8 --ignore=E402,F401,W391,W291,W293 twilio --max-line-length=300

test: install-dev analysis
	pytest

coverage: clean-pyc analysis
	pip install -q -e .[test]
	coverage run -m pytest tests
	coverage report
	coverage html

test-all: clean-pyc install-dev
	tox

clean-pyc:
	find . -name '*.pyc' -exec rm -f {} +
	find . -name '*.pyo' -exec rm -f {} +
	find . -name '*~' -exec rm -f {} +

