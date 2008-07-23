# -*- coding: utf-8 -*-
#
# Copyright 2007-2008 UNINETT AS
#
# This file is part of Network Administration Visualized (NAV)
#
# NAV is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# NAV is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with NAV; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# Authors: Magnus Motzfeldt Eide <magnus.eide@uninett.no>
#

__copyright__ = "Copyright 2007-2008 UNINETT AS"
__license__ = "GPL"
__author__ = "Magnus Motzfeldt Eide (magnus.eide@uninett.no)"
__id__ = "$Id$"

from django.conf.urls.defaults import *

from nav.web.alertprofiles.views import *

# The patterns are relative to the base URL of the subsystem
urlpatterns = patterns('',
    # Overview
    url(r'^$', overview,
        name='alertprofiles-overview'),

    # User settings
    url(r'^profile/$', profile,
        name='alertprofiles-profile'),
    url(r'^profile/page(?P<page>[0-9]+)/$', profile,
        name='alertprofiles-profile-list'),
    url(r'^profile/new/$', profile_new,
        name='alertprofiles-profile-new'),
    url(r'^profile/(?P<profile_id>\d+)/$', profile_detail,
        name='alertprofiles-profile-detail'),
    url(r'^profile/save/$', profile_save,
        name='alertprofiles-profile-save'),
    url(r'^profile/remove/$', profile_remove,
        name='alertprofiles-profile-remove'),

    url(r'^profile/time_period/(?P<time_period_id>\d+)/$', profile_time_period_setup,
        name='alertprofiles-profile-timeperiod-setup'),
    url(r'^profile/time_period/add/$', profile_time_period_add,
        name='alertprofiles-profile-timeperiod-add'),
    url(r'^profile/time_period/remove/$', profile_time_period_remove,
        name='alertprofiles-profile-timeperiod-remove'),

    url(r'^profile/time_period/subscription/(?P<subscription_id>\d+)$', profile_time_period_subscription_edit,
        name='alertprofiles-profile-timeperiod-subscription'),
    url(r'^profile/time_period/subscription/add/$', profile_time_period_subscription_add,
        name='alertprofiles-profile-timeperiod-subscription-add'),
    url(r'^profile/time_period/subscription/remove/$', profile_time_period_subscription_remove,
        name='alertprofiles-profile-timeperiod-subscription-remove'),

    url(r'^language/save/$', language_save,
        name='alertprofiles-language-save'),

    url(r'^sms/$', sms_list,
        name='alertprofiles-sms'),
    url(r'^sms/page(?P<page>[0-9]+)/$', sms_list,
        name='alertprofiles-sms-list'),

    # Alert address
    url(r'^address/$', address_list,
        name='alertprofiles-address'),
    url(r'^address/page(?P<page>[0-9]+)/$', address_list,
        name='alertprofiles-address-list'),
    url(r'^address/(?P<address_id>\d+)/$', address_detail,
        name='alertprofiles-address-detail'),
    url(r'^address/new/$', address_detail,
        name='alertprofiles-address-new'),
    url(r'^address/save/$', address_save,
        name='alertprofiles-address-save'),
    url(r'^address/remove/$', address_remove,
        name='alertprofiles-address-remove'),

    # Filters
    url(r'^filters/$', filter_list,
        name='alertprofiles-filters'),
    url(r'^filters/page(?P<page>[0-9]+)/$', filter_list,
        name='alertprofiles-filters-list'),
    url(r'^filters/(?P<filter_id>\d+)/$', filter_detail,
        name='alertprofiles-filters-detail'),
    url(r'^filters/new/$', filter_detail,
        name='alertprofiles-filters-new'),
    url(r'^filters/save/$', filter_save,
        name='alertprofiles-filters-save'),
    url(r'^filters/remove/$', filter_remove,
        name='alertprofiles-filters-remove'),
    url(r'^filters/addexpresion/$', filter_addexpresion,
        name='alertprofiles-filters-addexpresion'),
    url(r'^filters/saveexpresion/$', filter_saveexpresion,
        name='alertprofiles-filters-saveexpresion'),
    url(r'^filters/removeexpresion/$', filter_removeexpresion,
        name='alertprofiles-filters-removeexpresion'),

    # Filter groups
    url(r'^filter-groups/$', filtergroup_list,
        name='alertprofiles-filtergroups'),
    url(r'^filter-groups/page(?P<page>[0-9]+)/$', filtergroup_list,
        name='alertprofiles-filtergroups-list'),
    url(r'^filter-groups/(?P<filter_group_id>\d+)/$', filtergroup_detail,
        name='alertprofiles-filtergroups-detail'),
    url(r'^filter-groups/new/$', filtergroup_detail,
        name='alertprofiles-filtergroups-new'),
    url(r'^filter-groups/save/$', filtergroup_save,
        name='alertprofiles-filtergroups-save'),
    url(r'^filter-groups/remove/$', filtergroup_remove,
        name='alertprofiles-filtergroups-remove'),
    url(r'^filter-groups/addfilter/$', filtergroup_addfilter,
        name='alertprofiles-filtergroups-addfilter'),
    url(r'^filter-groups/removefilter/$', filtergroup_removefilter,
        name='alertprofiles-filtergroups-removefilter'),

    # Filter variables (aka. matchfields)
    url(r'^matchfields/$', matchfield_list,
        name='alertprofiles-matchfields'),
    url(r'^matchfields/page(?P<page>[0-9]+)/$', matchfield_list,
        name='alertprofiles-matchfields-list'),
    url(r'^matchfields/(?P<matchfield_id>\d+)/$', matchfield_detail,
        name='alertprofiles-matchfields-detail'),
    url(r'^matchfields/new/$', matchfield_detail,
        name='alertprofiles-matchfields-new'),
    url(r'^matchfields/save/$', matchfield_save,
        name='alertprofiles-matchfields-save'),
    url(r'^matchfields/remove/$', matchfield_remove,
        name='alertprofiles-matchfields-remove'),

    # Admin settings:
    #################

    # Permissions
    url(r'^permissions/$', permission_list,
        name='alertprofiles-permissions'),
    url(r'^permissions/(?P<group_id>\d+)/$', permission_list,
        name='alertprofiles-permissions-detail'),
    url(r'^permissions/save/$', permissions_save,
        name='alertprofiles-permissions-save'),
)
