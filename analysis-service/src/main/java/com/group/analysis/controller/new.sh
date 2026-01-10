curl -X POST "http://localhost:8081/viewer/donate" ^
-H "Content-Type: application/json" ^
-d "{\"anchorId\": 1, \"anchorName\": \"TestAnchor\", \"viewerId\": 1001, \"viewerName\": \"TestViewer\", \"viewerGender\": 1, \"amount\": 100.00}"