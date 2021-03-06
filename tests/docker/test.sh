#!/bin/bash -xe

run_jstests() {
    /javascript-tests.sh
}

run_pylint() {
    time "/pylint.sh" > "${WORKSPACE}/pylint.txt"
}

dump_possibly_relevant_apache_accesses() {
    set +x
    ACCESSLOG="${BUILDDIR}/var/log/apache2-access.log"
    if [ -e "$ACCESSLOG" ]; then
        echo "Potentially relevant 40x errors from Apache logs:"
	echo "-------------------------------------------------"
	grep " 40[34] " "$ACCESSLOG"
	echo "-------------------------------------------------"
    fi
}


# MAIN EXECUTION POINT
cd "$WORKSPACE"
/build.sh

# Run unit tests before starting services
/python-unit-tests.sh

# start services
/create-db.sh
/start-services.sh
trap dump_possibly_relevant_apache_accesses EXIT

# Run integrations tests after everything is up
/integration-tests.sh
if ! /functional-tests.sh
then
    echo "Functional tests failed. Dumping error log from apache"

    echo "-------------------------------------------------"
    cat "${BUILDDIR}/var/log/apache2-error.log"
    echo "-------------------------------------------------"
    exit 1
fi

run_jstests

# Code analysis steps
run_pylint
/count-lines-of-code.sh

echo "test.sh done"
