#
# Copyright (C) 2009-2013 UNINETT AS
#
# This file is part of Network Administration Visualized (NAV).
#
# NAV is free software: you can redistribute it and/or modify it under
# the terms of the GNU General Public License version 2 as published by
# the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.  You should have received a copy of the GNU General Public License
# along with NAV. If not, see <http://www.gnu.org/licenses/>.
#
"""Common utility functions for Machine Tracker"""

from datetime import datetime
from socket import gethostbyaddr, herror
from IPy import IP

from nav import asyncdns
from nav.models.manage import Prefix
from nav.ipdevpoll.db import commit_on_success

from django.utils.datastructures import SortedDict
from django.db import DatabaseError

_cached_hostname = {}


def hostname(ip):
    """
    Performs a DNS reverse lookup for an IP address and caches the result in
    a global variable, which is really, really stupid.

    :param ip: And IP address string.
    :returns: A hostname string or a False value if the lookup failed.

    """
    addr = unicode(ip)
    if addr in _cached_hostname:
        return _cached_hostname[addr]

    try:
        dns = gethostbyaddr(addr)
    except herror:
        return False

    _cached_hostname[addr] = dns[0]
    return dns[0]


@commit_on_success
def get_prefix_info(addr):
    """Returns the smallest prefix from the NAVdb that an IP address fits into.

    :param addr: An IP address string.
    :returns: A Prefix object or None if no prefixes matched.

    """
    try:
        return Prefix.objects.select_related().extra(
            select={"mask_size": "masklen(netaddr)"},
            where=["%s << netaddr AND nettype <> 'scope'"],
            order_by=["-mask_size"],
            params=[addr]
        )[0]
    except (IndexError, DatabaseError):
        return None


def normalize_ip_to_string(ipaddr):
    """Normalizes an IP address to a a sortable string.

    When sending IP addresses to a browser and asking JavaScript to sort them
    as strings, this function will help.

    An IPv4 address will be normalized to '4' + <15-character dotted quad>.
    An IPv6 address will be normalized to '6' + <39 character IPv6 address>

    """
    try:
        ipaddr = IP(ipaddr)
    except ValueError:
        return ipaddr

    if ipaddr.version() == 4:
        quad = str(ipaddr).split('.')
        return '4%s' % '.'.join([i.zfill(3) for i in quad])
    else:
        return '6%s' % ipaddr.strFullsize()


def ip_dict(rows):
    """Converts IP search result rows to a dict keyed by IP addresses.

    :param rows: IP search result rows.
    :return: A dict mapping IP addresses to matching result rows.
    """
    result = SortedDict()
    for row in rows:
        ip = IP(row.ip)
        if ip not in result:
            result[ip] = []
        result[ip].append(row)
    return result


def process_ip_row(row, dns):
    """Processes an IP search result row"""
    if row.end_time > datetime.now():
        row.still_active = "Still active"
    if dns:
        row.dns_lookup = hostname(row.ip) or ""
    return row


def min_max_mac(prefix):
    """Finds the minimum and maximum MAC addresses of a given prefix.

    :returns: A tuple of (min_mac_string, max_mac_string)

    """
    return unicode(prefix[0]), unicode(prefix[-1])


def track_mac(keys, resultset, dns):
    """Groups results from Query for the mac_search page.

        keys        - a tuple/list with strings that identifies the fields the
                      result should be grouped by
        resultset   - a QuerySet
        dns         - should we lookup the hostname?
    """
    if dns:
        ips_to_lookup = [row['ip'] for row in resultset]
        dns_lookups = asyncdns.reverse_lookup(ips_to_lookup)

    tracker = SortedDict()
    for row in resultset:
        if row['end_time'] > datetime.now():
            row['still_active'] = "Still active"
        if dns:
            if dns_lookups[row.ip] and not isinstance(dns_lookups[row.ip], Exception):
                row.dns_lookup = dns_lookups[row.ip].pop()
            else:
                row['dns_lookup'] = ""
        if 'module' not in row or not row['module']:
            row['module'] = ''
        if 'port' not in row or not row['port']:
            row['port'] = ''
        key = []
        for k in keys:
            key.append(row.get(k))
        key = tuple(key)
        if key not in tracker:
            tracker[key] = []
        tracker[key].append(row)
    return tracker


class ProcessInput:
    """Some sort of search form input processing class. Who the hell knows."""
    def __init__(self, input):
        self.input = input.copy()

    def __common(self):
        if not self.input.get('days', False):
            self.input['days'] = 7

    def __prefix(self):
        try:
            ip = Prefix.objects.get(id=self.input['prefixid'])
        except Prefix.DoesNotExist:
            return None
        self.input['ip_range'] = ip.net_address

    def ip(self):
        if self.input.get('prefixid', False):
            self.__prefix()
        self.__common()
        if not (self.input.get('active', False)
                or self.input.get('inactive', False)):
            self.input['active'] = "on"
        return self.input

    def mac(self):
        self.__common()
        return self.input

    def swp(self):
        self.__common()
        return self.input

    def netbios(self):
        self.__common()
        return self.input
