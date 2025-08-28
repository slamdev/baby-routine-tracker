const admin = require('firebase-admin');

// Initialize Firebase Admin (requires service account key)
// Make sure to set GOOGLE_APPLICATION_CREDENTIALS environment variable
admin.initializeApp();
const db = admin.firestore();

// Emma's data
const BABY_ID = '8ea89dfd-3997-4323-8bd9-6b7843a5b3ae';
const USER_ID = 'jNKhIdUEWicnTf7bd88zFRTSNl83';

// Date range: last 2 months from today
const endDate = new Date();
const startDate = new Date();
startDate.setMonth(startDate.getMonth() - 2);

console.log(`Generating data for Emma from ${startDate.toDateString()} to ${endDate.toDateString()}`);

// Helper functions
function randomBetween(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function addMinutes(date, minutes) {
    return new Date(date.getTime() + minutes * 60000);
}

function addHours(date, hours) {
    return new Date(date.getTime() + hours * 60 * 60000);
}

// Generate typical 4-month-old patterns
function generateDayData(date) {
    const activities = [];
    let currentTime = new Date(date);
    currentTime.setHours(6, 0, 0, 0); // Start at 6 AM
    
    // Morning routine (6 AM - 12 PM)
    // Wake up feeding
    const morningFeedingType = Math.random() < 0.6 ? 'breast_milk' : 'bottle';
    const morningFeeding = {
        type: 'FEEDING',
        feedingType: morningFeedingType,
        startTime: admin.firestore.Timestamp.fromDate(new Date(currentTime)),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(new Date(currentTime)),
        updatedAt: admin.firestore.Timestamp.fromDate(new Date(currentTime))
    };
    
    if (morningFeedingType === 'breast_milk') {
        // Breast feeding: 15-25 minutes
        const duration = randomBetween(15, 25);
        morningFeeding.endTime = admin.firestore.Timestamp.fromDate(addMinutes(currentTime, duration));
    } else {
        // Bottle feeding: instant with amount
        morningFeeding.endTime = admin.firestore.Timestamp.fromDate(new Date(currentTime));
        morningFeeding.amount = randomBetween(120, 180); // ml
        if (Math.random() < 0.3) {
            morningFeeding.notes = ['Good appetite', 'Finished bottle', 'A bit fussy'][randomBetween(0, 2)];
        }
    }
    activities.push(morningFeeding);
    currentTime = addMinutes(currentTime, 30);
    
    // Morning nap (7:30-9:30 AM)
    const morningNapStart = new Date(currentTime);
    const morningNapDuration = randomBetween(90, 150); // 1.5-2.5 hours
    activities.push({
        type: 'SLEEP',
        startTime: admin.firestore.Timestamp.fromDate(morningNapStart),
        endTime: admin.firestore.Timestamp.fromDate(addMinutes(morningNapStart, morningNapDuration)),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(morningNapStart),
        updatedAt: admin.firestore.Timestamp.fromDate(morningNapStart)
    });
    currentTime = addMinutes(morningNapStart, morningNapDuration + 15);
    
    // Mid-morning feeding (10 AM)
    const midMorningFeedingType = Math.random() < 0.5 ? 'breast_milk' : 'bottle';
    const midMorningFeeding = {
        type: 'FEEDING',
        feedingType: midMorningFeedingType,
        startTime: admin.firestore.Timestamp.fromDate(new Date(currentTime)),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(new Date(currentTime)),
        updatedAt: admin.firestore.Timestamp.fromDate(new Date(currentTime))
    };
    
    if (midMorningFeedingType === 'breast_milk') {
        const duration = randomBetween(12, 20);
        midMorningFeeding.endTime = admin.firestore.Timestamp.fromDate(addMinutes(currentTime, duration));
    } else {
        midMorningFeeding.endTime = admin.firestore.Timestamp.fromDate(new Date(currentTime));
        midMorningFeeding.amount = randomBetween(100, 150);
        if (Math.random() < 0.2) {
            midMorningFeeding.notes = ['Sleepy during feeding', 'Only half bottle'][randomBetween(0, 1)];
        }
    }
    activities.push(midMorningFeeding);
    currentTime = addMinutes(currentTime, 30);
    
    // Afternoon routine (12 PM - 6 PM)
    // Lunch feeding
    const lunchFeedingType = Math.random() < 0.7 ? 'breast_milk' : 'bottle';
    const lunchFeeding = {
        type: 'FEEDING',
        feedingType: lunchFeedingType,
        startTime: admin.firestore.Timestamp.fromDate(addHours(currentTime, 1.5)),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(addHours(currentTime, 1.5)),
        updatedAt: admin.firestore.Timestamp.fromDate(addHours(currentTime, 1.5))
    };
    
    if (lunchFeedingType === 'breast_milk') {
        const duration = randomBetween(18, 25);
        lunchFeeding.endTime = admin.firestore.Timestamp.fromDate(addMinutes(addHours(currentTime, 1.5), duration));
    } else {
        lunchFeeding.endTime = admin.firestore.Timestamp.fromDate(addHours(currentTime, 1.5));
        lunchFeeding.amount = randomBetween(130, 180);
    }
    activities.push(lunchFeeding);
    
    // Afternoon nap (2 PM - 4 PM)
    const afternoonNapStart = addHours(currentTime, 3);
    const afternoonNapDuration = randomBetween(120, 180); // 2-3 hours
    activities.push({
        type: 'SLEEP',
        startTime: admin.firestore.Timestamp.fromDate(afternoonNapStart),
        endTime: admin.firestore.Timestamp.fromDate(addMinutes(afternoonNapStart, afternoonNapDuration)),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(afternoonNapStart),
        updatedAt: admin.firestore.Timestamp.fromDate(afternoonNapStart)
    });
    
    // Late afternoon feeding (4:30 PM)
    const lateAfternoonTime = addHours(currentTime, 5.5);
    const lateAfternoonFeedingType = Math.random() < 0.4 ? 'breast_milk' : 'bottle';
    const lateAfternoonFeeding = {
        type: 'FEEDING',
        feedingType: lateAfternoonFeedingType,
        startTime: admin.firestore.Timestamp.fromDate(lateAfternoonTime),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(lateAfternoonTime),
        updatedAt: admin.firestore.Timestamp.fromDate(lateAfternoonTime)
    };
    
    if (lateAfternoonFeedingType === 'breast_milk') {
        const duration = randomBetween(15, 22);
        lateAfternoonFeeding.endTime = admin.firestore.Timestamp.fromDate(addMinutes(lateAfternoonTime, duration));
    } else {
        lateAfternoonFeeding.endTime = admin.firestore.Timestamp.fromDate(lateAfternoonTime);
        lateAfternoonFeeding.amount = randomBetween(120, 160);
    }
    activities.push(lateAfternoonFeeding);
    
    // Evening routine (6 PM - 10 PM)
    // Short evening nap (6 PM - 7 PM)
    const eveningNapStart = addHours(currentTime, 7);
    const eveningNapDuration = randomBetween(30, 60); // 30-60 minutes
    activities.push({
        type: 'SLEEP',
        startTime: admin.firestore.Timestamp.fromDate(eveningNapStart),
        endTime: admin.firestore.Timestamp.fromDate(addMinutes(eveningNapStart, eveningNapDuration)),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(eveningNapStart),
        updatedAt: admin.firestore.Timestamp.fromDate(eveningNapStart)
    });
    
    // Dinner feeding (7:30 PM)
    const dinnerTime = addHours(currentTime, 8.5);
    const dinnerFeedingType = Math.random() < 0.8 ? 'breast_milk' : 'bottle';
    const dinnerFeeding = {
        type: 'FEEDING',
        feedingType: dinnerFeedingType,
        startTime: admin.firestore.Timestamp.fromDate(dinnerTime),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(dinnerTime),
        updatedAt: admin.firestore.Timestamp.fromDate(dinnerTime)
    };
    
    if (dinnerFeedingType === 'breast_milk') {
        const duration = randomBetween(20, 30);
        dinnerFeeding.endTime = admin.firestore.Timestamp.fromDate(addMinutes(dinnerTime, duration));
    } else {
        dinnerFeeding.endTime = admin.firestore.Timestamp.fromDate(dinnerTime);
        dinnerFeeding.amount = randomBetween(140, 200);
        if (Math.random() < 0.2) {
            dinnerFeeding.notes = 'Pre-bedtime feeding';
        }
    }
    activities.push(dinnerFeeding);
    
    // Night sleep (9 PM - 6 AM next day)
    const bedtime = addHours(currentTime, 10);
    const nightSleepDuration = randomBetween(480, 600); // 8-10 hours (with potential night wakings)
    activities.push({
        type: 'SLEEP',
        startTime: admin.firestore.Timestamp.fromDate(bedtime),
        endTime: admin.firestore.Timestamp.fromDate(addMinutes(bedtime, nightSleepDuration)),
        loggedBy: USER_ID,
        createdAt: admin.firestore.Timestamp.fromDate(bedtime),
        updatedAt: admin.firestore.Timestamp.fromDate(bedtime)
    });
    
    // Night feeding (random between 12 AM - 4 AM, 70% chance)
    if (Math.random() < 0.7) {
        const nightFeedingTime = addHours(bedtime, randomBetween(3, 7));
        const nightFeedingType = Math.random() < 0.9 ? 'breast_milk' : 'bottle'; // Mostly breast at night
        const nightFeeding = {
            type: 'FEEDING',
            feedingType: nightFeedingType,
            startTime: admin.firestore.Timestamp.fromDate(nightFeedingTime),
            loggedBy: USER_ID,
            createdAt: admin.firestore.Timestamp.fromDate(nightFeedingTime),
            updatedAt: admin.firestore.Timestamp.fromDate(nightFeedingTime)
        };
        
        if (nightFeedingType === 'breast_milk') {
            const duration = randomBetween(10, 20); // Shorter night feedings
            nightFeeding.endTime = admin.firestore.Timestamp.fromDate(addMinutes(nightFeedingTime, duration));
        } else {
            nightFeeding.endTime = admin.firestore.Timestamp.fromDate(nightFeedingTime);
            nightFeeding.amount = randomBetween(80, 120);
            nightFeeding.notes = 'Night feeding';
        }
        activities.push(nightFeeding);
    }
    
    // Diaper changes (2-4 times per day)
    const numDiaperChanges = randomBetween(2, 4);
    const diaperTimes = [];
    
    // Spread diaper changes throughout the day
    for (let i = 0; i < numDiaperChanges; i++) {
        const hour = 8 + (i * 4) + randomBetween(-1, 1); // Every 4 hours roughly
        const diaperTime = new Date(date);
        diaperTime.setHours(hour, randomBetween(0, 59), 0, 0);
        
        const diaper = {
            type: 'DIAPER',
            diaperType: 'poop',
            startTime: admin.firestore.Timestamp.fromDate(diaperTime),
            endTime: admin.firestore.Timestamp.fromDate(diaperTime),
            loggedBy: USER_ID,
            createdAt: admin.firestore.Timestamp.fromDate(diaperTime),
            updatedAt: admin.firestore.Timestamp.fromDate(diaperTime)
        };
        
        // Add notes occasionally
        if (Math.random() < 0.3) {
            const notes = [
                'Normal consistency', 
                'A bit runny', 
                'Good amount', 
                'Small amount',
                'Yellow color',
                'Diaper was full'
            ];
            diaper.notes = notes[randomBetween(0, notes.length - 1)];
        }
        
        activities.push(diaper);
    }
    
    return activities;
}

async function uploadData() {
    try {
        const batch = db.batch();
        let totalActivities = 0;
        
        // Generate data for each day in the range
        for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
            const dayData = generateDayData(new Date(d));
            
            for (const activity of dayData) {
                const activityRef = db.collection('babies').doc(BABY_ID).collection('activities').doc();
                batch.set(activityRef, activity);
                totalActivities++;
            }
            
            console.log(`Generated ${dayData.length} activities for ${d.toDateString()}`);
        }
        
        console.log(`\nUploading ${totalActivities} activities to Firebase...`);
        await batch.commit();
        console.log('✅ Data uploaded successfully!');
        
        // Generate summary
        console.log('\n📊 Data Summary:');
        console.log(`- Total activities: ${totalActivities}`);
        console.log(`- Date range: ${startDate.toDateString()} to ${endDate.toDateString()}`);
        console.log(`- Baby: Emma (${BABY_ID})`);
        console.log('- Activity types: Sleep, Breast Feeding, Bottle Feeding, Diaper Changes');
        console.log('- Pattern: Mixed feeding (60% breast, 40% bottle), 3-4 naps, 2-4 diaper changes per day');
        
    } catch (error) {
        console.error('❌ Error uploading data:', error);
    }
}

uploadData();
