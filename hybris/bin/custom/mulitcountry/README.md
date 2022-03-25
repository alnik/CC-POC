# SAP Commerce Multi-Country, Multi-Channel Product Enhancements Solution Add-On

## Documentation

* [Package Documentation](https://wiki.hybris.com/display/servportfolio/Multi-Country+Add-On+-+Consultant+Guide)

## Before using this package

This software is licensed under the *[SAP SAMPLE CODE LICENSE AGREEMENT](LICENSE.txt)*. In particular, SAP does not offer any support nor warranty for it, outside of consulting engagements.

This software must only be provided to partners or customers **as part of a Digital Business Service consulting engagement**, in order to ensure proper use and integration of the solution. Please read the [Solution Add-Ons FAQ](https://wiki.hybris.com/display/servportfolio/Services+Portfolio+-+Solution+Add-Ons+FAQ) or contact the Services Portfolio team if you are unsure about how this package can be used.

## Branching Strategy and Maintenance

    ----+------- master -------------+--------------------->
             \                                                    ^
              \                                                  /
               \--- fix/< release – fix name >----->
                \
	    \-- feature/-< release – feature name> ------>
                  \
                       `-- release/-<platform version> ------>


While the master branch contains the reference implementation with the latest functionality and is the one we maintain in-house, we store versions that belong to previous Hybris releases for historical and maintenance reasons.

### Guidelines

* The *master* is the reference branch (duh)
* It is not possible to commit directly to the *master*, every change has to be done from a branch
* Bugfix and Feature branches are kept separate:
* They store potentially specific configuratio	n values
* They store potentially specific formats or customizations
* Fix and Feature branches merge back into *master* through pull requests. For any changes that could be used by other projects, please create a pull request and address it to the Services Portfolio team (DL CEC Services Portfolio)
* Branches can also be created for initiatives or experiments using Feature branches
* When branching a Feature, please make sure to edit this README to clarify the specifics of the initiative. Namely:
* customer name (if it’s a customer-specific feature)
* location of wiki documentation
* Hybris and any dependency versions
* any relevant information about how it differs from the master
(probably best kept in the wiki, but store here if necessary)
* If the project is long-lived or requires an update, or if an
initiative is regularly maintained, it should sync up with the *master*.
* Create appropriate tags for meaningful releases of your branch,
e.g. when doing an upgrade for a specific component.
<s></s>

### Update 26/05/2011
This branch contains the modifications done to support Spartacus on 2011
It also includes the following bug fixes and featture
 - https://cxjira.sap.com/browse/EXTSERV-783 (Improvement for Excel export/import with user friendly format for ProductAvailabilityAssignment)
 - https://cxjira.sap.com/browse/EXTSERV-788 (Support Discounts - Adding discount price group to base store)
 - https://cxjira.sap.com/browse/EXTSERV-782 (Spartacus support on 2005 - product url, name, facets missing on list page)
 - https://cxjira.sap.com/browse/EXTSERV-781 (Spartacus Support on 2005 - Product image displayed in CMS Component for unavailable product)
 - Simple search not working on Product Backoffice (Use of And operator instead of OR for simple search on searchable fields :description, name, keyword, etc)