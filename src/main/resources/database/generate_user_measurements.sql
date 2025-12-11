-- ============================================
-- AUTO-GENERATE USER MEASUREMENTS FOR FIRST 100 USERS
-- ============================================
-- This script generates realistic random measurements for users
-- who don't have measurements yet.
--
-- Run this script to populate user_measurements table with diverse data
-- for testing the Size Recommendation algorithm.
-- ============================================

-- PostgreSQL function to generate random measurements
DO $$
DECLARE
    user_record RECORD;
    v_gender VARCHAR(10);
    v_age INT;
    v_height DECIMAL(5,2);
    v_weight DECIMAL(5,2);
    v_chest DECIMAL(5,2);
    v_waist DECIMAL(5,2);
    v_hips DECIMAL(5,2);
    v_bmi DECIMAL(4,2);
    v_belly_shape VARCHAR(10);
    v_hip_shape VARCHAR(10);
    v_chest_shape VARCHAR(10);
    v_fit_preference VARCHAR(15);
    v_random DECIMAL;
BEGIN
    -- Loop through first 100 users who don't have measurements yet
    FOR user_record IN 
        SELECT u.id 
        FROM users u 
        LEFT JOIN user_measurements um ON um.user_id = u.id
        WHERE um.id IS NULL
        ORDER BY u.id
        LIMIT 100
    LOOP
        -- Random gender (60% male, 40% female for clothing store)
        IF random() < 0.6 THEN
            v_gender := 'MALE';
        ELSE
            v_gender := 'FEMALE';
        END IF;
        
        -- Random age (18-55, weighted towards 20-35)
        v_age := 18 + floor(random() * 37)::int;
        IF random() < 0.6 THEN
            v_age := 20 + floor(random() * 15)::int; -- 20-35 more common
        END IF;
        
        -- Generate measurements based on gender
        IF v_gender = 'MALE' THEN
            -- Male measurements (Vietnamese average + variation)
            v_height := 165 + (random() * 20)::decimal(5,2);  -- 165-185 cm
            v_weight := 55 + (random() * 35)::decimal(5,2);   -- 55-90 kg
            v_chest := 88 + (random() * 20)::decimal(5,2);    -- 88-108 cm
            v_waist := 72 + (random() * 22)::decimal(5,2);    -- 72-94 cm
            v_hips := 88 + (random() * 18)::decimal(5,2);     -- 88-106 cm
        ELSE
            -- Female measurements (Vietnamese average + variation)
            v_height := 155 + (random() * 18)::decimal(5,2);  -- 155-173 cm
            v_weight := 45 + (random() * 25)::decimal(5,2);   -- 45-70 kg
            v_chest := 80 + (random() * 16)::decimal(5,2);    -- 80-96 cm
            v_waist := 62 + (random() * 18)::decimal(5,2);    -- 62-80 cm
            v_hips := 86 + (random() * 18)::decimal(5,2);     -- 86-104 cm
        END IF;
        
        -- Calculate BMI
        v_bmi := (v_weight / ((v_height/100) * (v_height/100)))::decimal(4,2);
        
        -- Clamp BMI to valid range
        IF v_bmi < 15 THEN v_bmi := 15; END IF;
        IF v_bmi > 40 THEN v_bmi := 40; END IF;
        
        -- Random belly shape (weighted)
        v_random := random();
        IF v_random < 0.3 THEN
            v_belly_shape := 'FLAT';
        ELSIF v_random < 0.75 THEN
            v_belly_shape := 'NORMAL';
        ELSE
            v_belly_shape := 'ROUND';
        END IF;
        
        -- Random hip shape (weighted)
        v_random := random();
        IF v_random < 0.25 THEN
            v_hip_shape := 'NARROW';
        ELSIF v_random < 0.75 THEN
            v_hip_shape := 'NORMAL';
        ELSE
            v_hip_shape := 'WIDE';
        END IF;
        
        -- Random chest shape (weighted)
        v_random := random();
        IF v_random < 0.25 THEN
            v_chest_shape := 'SLIM';
        ELSIF v_random < 0.75 THEN
            v_chest_shape := 'NORMAL';
        ELSE
            v_chest_shape := 'BROAD';
        END IF;
        
        -- Random fit preference (weighted towards COMFORTABLE)
        v_random := random();
        IF v_random < 0.2 THEN
            v_fit_preference := 'TIGHT';
        ELSIF v_random < 0.75 THEN
            v_fit_preference := 'COMFORTABLE';
        ELSE
            v_fit_preference := 'LOOSE';
        END IF;
        
        -- Insert measurement
        INSERT INTO user_measurements (
            user_id, gender, age, height, weight, chest, waist, hips, bmi,
            belly_shape, hip_shape, chest_shape, fit_preference, has_return_history,
            created_at, updated_at
        ) VALUES (
            user_record.id, v_gender, v_age, v_height, v_weight, v_chest, v_waist, v_hips, v_bmi,
            v_belly_shape, v_hip_shape, v_chest_shape, v_fit_preference, false,
            NOW(), NOW()
        )
        ON CONFLICT (user_id) DO NOTHING;
        
        RAISE NOTICE 'Created measurements for user_id: %', user_record.id;
    END LOOP;
END $$;

-- Verify results
SELECT 
    COUNT(*) as total_measurements,
    COUNT(CASE WHEN gender = 'MALE' THEN 1 END) as male_count,
    COUNT(CASE WHEN gender = 'FEMALE' THEN 1 END) as female_count,
    ROUND(AVG(age), 1) as avg_age,
    ROUND(AVG(bmi), 2) as avg_bmi
FROM user_measurements;

-- Show distribution by fit preference
SELECT 
    fit_preference,
    COUNT(*) as count,
    ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER(), 1) as percentage
FROM user_measurements
GROUP BY fit_preference
ORDER BY count DESC;

-- Show sample data
SELECT 
    user_id, gender, age, height, weight, bmi, 
    belly_shape, hip_shape, chest_shape, fit_preference
FROM user_measurements
ORDER BY user_id
LIMIT 20;
