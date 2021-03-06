#!/usr/bin/env python
import os
import sys
sys.path.insert(0, os.path.join(os.path.dirname(os.path.realpath(__file__)),
                                "../.."))
import nav.models  # This MUST be here to avoid a strange deadlock in Django

if __name__ == "__main__":
    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "nav.django.settings")

    from django.core.management import execute_from_command_line

    execute_from_command_line(sys.argv)
