const { https, functions } = require('firebase-functions');
const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
+++++++
```

// Initialize Firebase Admin
admin.initializeApp();

const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

// ============================================
// API Endpoints
// ============================================

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// ============================================
// User Routes
// ============================================

// Get user profile
app.get('/users/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        
        // Verify ID token
        const idToken = req.headers.authorization?.split(' ')[1];
        if (!idToken) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        
        // Only allow accessing own profile or admin access
        if (decodedToken.uid !== userId) {
            return res.status(403).json({ error: 'Forbidden' });
        }
        
        const userDoc = await admin.firestore().collection('users').doc(userId).get();
        
        if (!userDoc.exists) {
            return res.status(404).json({ error: 'User not found' });
        }
        
        res.json({ user: userDoc.data() });
    } catch (error) {
        console.error('Error getting user:', error);
        res.status(500).json({ error: error.message });
    }
});

// Update user profile
app.put('/users/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const userData = req.body;
        
        // Verify ID token
        const idToken = req.headers.authorization?.split(' ')[1];
        if (!idToken) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        
        // Only allow updating own profile
        if (decodedToken.uid !== userId) {
            return res.status(403).json({ error: 'Forbidden' });
        }
        
        await admin.firestore().collection('users').doc(userId).update(userData);
        
        const userDoc = await admin.firestore().collection('users').doc(userId).get();
        res.json({ user: userDoc.data() });
    } catch (error) {
        console.error('Error updating user:', error);
        res.status(500).json({ error: error.message });
    }
});

// ============================================
// Horse Routes
// ============================================

// Get user's horses
app.get('/users/:userId/horses', async (req, res) => {
    try {
        const { userId } = req.params;
        
        // Verify ID token
        const idToken = req.headers.authorization?.split(' ')[1];
        if (!idToken) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        
        // Only allow accessing own horses
        if (decodedToken.uid !== userId) {
            return res.status(403).json({ error: 'Forbidden' });
        }
        
        const horsesRef = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('horses')
            .get();
        
        const horses = horsesRef.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        }));
        
        res.json({ horses });
    } catch (error) {
        console.error('Error getting horses:', error);
        res.status(500).json({ error: error.message });
    }
});

// Add horse for user
app.post('/users/:userId/horses', async (req, res) => {
    try {
        const { userId } = req.params;
        const horseData = req.body;
        
        // Verify ID token
        const idToken = req.headers.authorization?.split(' ')[1];
        if (!idToken) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        
        // Only allow adding horses to own profile
        if (decodedToken.uid !== userId) {
            return res.status(403).json({ error: 'Forbidden' });
        }
        
        const horseRef = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('horses')
            .add(horseData);
        
        const horseDoc = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('horses')
            .doc(horseRef.id).get();
        
        res.status(201).json({ horse: { id: horseRef.id, ...horseDoc.data() } });
    } catch (error) {
        console.error('Error adding horse:', error);
        res.status(500).json({ error: error.message });
    }
});

// ============================================
// Session/Ride Routes
// ============================================

// Get user's sessions
app.get('/users/:userId/sessions', async (req, res) => {
    try {
        const { userId } = req.params;
        const { limit = 50, offset = 0 } = req.query;
        
        // Verify ID token
        const idToken = req.headers.authorization?.split(' ')[1];
        if (!idToken) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        
        // Only allow accessing own sessions
        if (decodedToken.uid !== userId) {
            return res.status(403).json({ error: 'Forbidden' });
        }
        
        const sessionsRef = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('sessions')
            .orderBy('createdAt', 'desc')
            .limit(parseInt(limit))
            .offset(parseInt(offset))
            .get();
        
        const sessions = sessionsRef.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        }));
        
        res.json({ sessions, count: sessions.length });
    } catch (error) {
        console.error('Error getting sessions:', error);
        res.status(500).json({ error: error.message });
    }
});

// Create new session
app.post('/users/:userId/sessions', async (req, res) => {
    try {
        const { userId } = req.params;
        const sessionData = req.body;
        
        // Verify ID token
        const idToken = req.headers.authorization?.split(' ')[1];
        if (!idToken) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        
        // Only allow creating sessions for own profile
        if (decodedToken.uid !== userId) {
            return res.status(403).json({ error: 'Forbidden' });
        }
        
        const sessionRef = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('sessions')
            .add({
                ...sessionData,
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            });
        
        const sessionDoc = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('sessions')
            .doc(sessionRef.id).get();
        
        res.status(201).json({ session: { id: sessionRef.id, ...sessionDoc.data() } });
    } catch (error) {
        console.error('Error creating session:', error);
        res.status(500).json({ error: error.message });
    }
});

// ============================================
// Ride Metrics Cloud Functions
// ============================================

// Calculate ride metrics from GPS data
const calculateRideMetrics = async (pathPoints, weightKg) => {
    if (!pathPoints || pathPoints.length < 2) {
        return { distanceKm: 0, durationSec: 0, calories: 0 };
    }
    
    // Calculate distance from GPS points
    let totalDistance = 0;
    for (let i = 1; i < pathPoints.length; i++) {
        const point1 = pathPoints[i - 1];
        const point2 = pathPoints[i];
        
        // Haversine formula for distance between two coordinates
        const R = 6371; // Earth's radius in km
        const dLat = (point2.latitude - point1.latitude) * Math.PI / 180;
        const dLon = (point2.longitude - point1.longitude) * Math.PI / 180;
        const a = 
            Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(point1.latitude * Math.PI / 180) * 
            Math.cos(point2.latitude * Math.PI / 180) * 
            Math.sin(dLon/2) * Math.sin(dLon/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        totalDistance += R * c;
    }
    
    // Calculate duration from timestamps
    const startTime = pathPoints[0].timestamp;
    const endTime = pathPoints[pathPoints.length - 1].timestamp;
    const durationSec = Math.floor((endTime - startTime) / 1000);
    
    // Calculate calories (approximate formula)
    // Calories = Distance(km) × Weight(kg) × MET value
    // Horseback riding MET ≈ 5.5
    const metValue = 5.5;
    const calories = Math.round(totalDistance * weightKg * metValue);
    
    // Calculate average speed
    const durationHours = durationSec / 3600;
    const avgSpeed = durationHours > 0 ? totalDistance / durationHours : 0;
    
    return {
        distanceKm: Math.round(totalDistance * 100) / 100,
        durationSec,
        calories,
        avgSpeed: Math.round(avgSpeed * 10) / 10
    };
};

// Cloud Function: Calculate Ride Metrics
exports.calculateRideMetrics = functions.https.onCall(async (data, context) => {
    const { pathPoints, weightKg } = data;
    
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User not authenticated');
    }
    
    if (!pathPoints || !Array.isArray(pathPoints)) {
        throw new functions.https.HttpsError('invalid-argument', 'pathPoints is required');
    }
    
    try {
        const metrics = calculateRideMetrics(pathPoints, weightKg || 70);
        return { success: true, metrics };
    } catch (error) {
        console.error('Error calculating ride metrics:', error);
        throw new functions.https.HttpsError('internal', 'Failed to calculate metrics');
    }
});

// Cloud Function: Save Ride Session
exports.saveRideSession = functions.https.onCall(async (data, context) => {
    const { sessionData } = data;
    
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User not authenticated');
    }
    
    if (!sessionData) {
        throw new functions.https.HttpsError('invalid-argument', 'sessionData is required');
    }
    
    try {
        const userId = context.auth.uid;
        
        // Calculate metrics from path points if available
        if (sessionData.pathPoints && Array.isArray(sessionData.pathPoints)) {
            const metrics = calculateRideMetrics(
                sessionData.pathPoints,
                sessionData.weightKg || 70
            );
            
            // Update session with calculated metrics if not already present
            if (!sessionData.distanceKm) {
                sessionData.distanceKm = metrics.distanceKm;
            }
            if (!sessionData.durationSec) {
                sessionData.durationSec = metrics.durationSec;
            }
            if (!sessionData.calories) {
                sessionData.calories = metrics.calories;
            }
            if (!sessionData.avgSpeed) {
                sessionData.avgSpeed = metrics.avgSpeed;
            }
        }
        
        // Set timestamps
        sessionData.createdAt = admin.firestore.FieldValue.serverTimestamp();
        
        const sessionRef = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('sessions')
            .add(sessionData);
        
        const sessionDoc = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('sessions')
            .doc(sessionRef.id).get();
        
        return { 
            success: true, 
            session: { id: sessionRef.id, ...sessionDoc.data() },
            metrics: {
                distanceKm: sessionData.distanceKm,
                durationSec: sessionData.durationSec,
                calories: sessionData.calories
            }
        };
    } catch (error) {
        console.error('Error saving ride session:', error);
        throw new functions.https.HttpsError('internal', 'Failed to save session');
    }
});

// Cloud Function: Get Ride History
exports.getRideHistory = functions.https.onCall(async (data, context) => {
    const { limit = 50, offset = 0 } = data;
    
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User not authenticated');
    }
    
    try {
        const userId = context.auth.uid;
        
        const sessionsRef = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('sessions')
            .orderBy('createdAt', 'desc')
            .limit(parseInt(limit))
            .offset(parseInt(offset))
            .get();
        
        const sessions = await Promise.all(sessionsRef.docs.map(async (doc) => {
            const sessionData = doc.data();
            
            // Calculate metrics if pathPoints available
            if (sessionData.pathPoints && Array.isArray(sessionData.pathPoints)) {
                const metrics = calculateRideMetrics(
                    sessionData.pathPoints,
                    sessionData.weightKg || 70
                );
                
                return {
                    id: doc.id,
                    ...sessionData,
                    calculatedMetrics: metrics
                };
            }
            
            return {
                id: doc.id,
                ...sessionData
            };
        }));
        
        return { 
            success: true, 
            sessions,
            count: sessions.length
        };
    } catch (error) {
        console.error('Error getting ride history:', error);
        throw new functions.https.HttpsError('internal', 'Failed to get ride history');
    }
});

// Cloud Function: Get User Statistics
exports.getUserStatistics = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User not authenticated');
    }
    
    try {
        const userId = context.auth.uid;
        
        // Get all sessions for this user
        const sessionsRef = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('sessions')
            .get();
        
        const sessions = sessionsRef.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        }));
        
        // Calculate statistics
        const totalRides = sessions.length;
        const totalTime = sessions.reduce((sum, s) => sum + (s.durationSec || 0), 0);
        const totalDistance = sessions.reduce((sum, s) => sum + (s.distanceKm || 0), 0);
        const totalCalories = sessions.reduce((sum, s) => sum + (s.calories || 0), 0);
        
        // Calculate average metrics
        const avgSpeed = totalTime > 0 ? totalDistance / (totalTime / 3600) : 0;
        const avgDuration = totalTime / totalRides || 0;
        const avgDistance = totalRides > 0 ? totalDistance / totalRides : 0;
        
        // Calculate weekly trend (last 7 days)
        const now = new Date();
        const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        
        const dailyDistances = [0, 0, 0, 0, 0, 0, 0];
        sessions.forEach(session => {
            if (session.createdAt) {
                const sessionDate = new Date(session.createdAt);
                const diffTime = now - sessionDate;
                const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
                
                if (diffDays >= 0 && diffDays < 7) {
                    dailyDistances[6 - diffDays] += (session.distanceKm || 0);
                }
            }
        });
        
        return {
            success: true,
            statistics: {
                totalRides,
                totalTime: Math.round(totalTime / 3600), // hours
                totalDistance: Math.round(totalDistance * 10) / 10,
                totalCalories,
                avgSpeed: Math.round(avgSpeed * 10) / 10,
                avgDuration: Math.round(avgDuration / 60), // minutes
                avgDistance: Math.round(avgDistance * 10) / 10,
                weeklyTrend: dailyDistances.map(d => Math.round(d * 10) / 10)
            }
        };
    } catch (error) {
        console.error('Error getting user statistics:', error);
        throw new functions.https.HttpsError('internal', 'Failed to get user statistics');
    }
});

// ============================================
// Export API and Functions
// ============================================

module.exports = {
    api: app,
    calculateRideMetrics,
    saveRideSession,
    getRideHistory,
    getUserStatistics
};
