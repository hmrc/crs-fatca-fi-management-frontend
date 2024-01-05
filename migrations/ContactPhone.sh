#!/bin/bash

echo ""
echo "Applying migration ContactPhone"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /contactPhone                        controllers.ContactPhoneController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /contactPhone                        controllers.ContactPhoneController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeContactPhone                  controllers.ContactPhoneController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeContactPhone                  controllers.ContactPhoneController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "contactPhone.title = contactPhone" >> ../conf/messages.en
echo "contactPhone.heading = contactPhone" >> ../conf/messages.en
echo "contactPhone.checkYourAnswersLabel = contactPhone" >> ../conf/messages.en
echo "contactPhone.error.required = Enter contactPhone" >> ../conf/messages.en
echo "contactPhone.error.length = ContactPhone must be 135 characters or less" >> ../conf/messages.en
echo "contactPhone.change.hidden = ContactPhone" >> ../conf/messages.en

echo "Migration ContactPhone completed"
