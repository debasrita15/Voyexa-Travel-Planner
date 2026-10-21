<h1 align="center">🌍 Voyexa: GenAI Travel Planner & Guide Marketplace</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9%2B-blue"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-UI-green"/>
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-orange"/>
  <img src="https://img.shields.io/badge/AI-Groq%20%7C%20LLaMA%203.3%2070B-purple"/>
  <img src="https://img.shields.io/badge/Backend-Supabase-blue"/>
  <img src="https://img.shields.io/badge/Database-Room%20SQLite-red"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg"/>
</p>

<p align="center">
  ⚡ Built an ultra-low latency AI travel platform connecting Travellers & Guides using Groq API with LLaMA 3.3 70B inference
</p>

<hr/>

<h2>✨ Overview</h2>

<p>
<b>Voyexa</b> is a modern, full-stack Android application that combines <b>real-time GenAI travel intelligence</b> with a <b>dual-sided marketplace</b> connecting Travellers and verified Local Tour Guides.
</p>

<p>
Built with <b>100% Jetpack Compose + Clean MVVM Architecture</b>, Voyexa delivers location-aware demand forecasts, crowd-beating route optimizations, instant booking sync, and real-time chat with persistent cloud and local storage.
</p>

<hr/>

<h2>📲 Download APK</h2>

<ul>
  <li>👉 <b>Latest Official Release:</b> 
    <a href="https://github.com/debasrita15/Voyexa-Travel-Planner/releases/tag/v1.0.0">Voyexa v1.0.0 Release</a>
  </li>

  <li>📦 <b>Direct APK Download:</b> 
    <a href="https://github.com/debasrita15/Voyexa-Travel-Planner/releases/tag/v1.0.0">Download Voyexa APK</a>
  </li>
</ul>

<h3>📱 How to install on Android</h3>

<ol>
  <li>Download the APK from the release link above</li>
  <li>Open the downloaded <code>.apk</code> file on your Android device</li>
  <li>If prompted, enable <b>“Install unknown apps”</b> permission for your browser/file manager</li>
  <li>Tap <b>Install</b> 🚀</li>
  <li>Open Voyexa and start exploring</li>
</ol>

<hr/>

<h2>💡 Why Voyexa?</h2>

<p>
This project demonstrates how cutting-edge mobile architecture, sub-second LLM inference (Groq), and modern cloud backends (Supabase) can be synthesized to build a production-grade, offline-resilient Android application.
</p>

<hr/>

<h2>🎯 Problem Statement</h2>

<p>
Traditional travel platforms separate itinerary creation from local guide hiring and lack intelligent real-time demand forecasting for tour operators.
</p>

<p><b>Voyexa solves this by:</b></p>

<ul>
  <li>Bridging travellers and guides on a single unified platform with location-aware matching</li>
  <li>Providing local tour guides with real-time AI travel demand forecasts and pricing suggestions</li>
  <li>Generating crowd-beating smart route itineraries and storytelling talking points via Groq LLaMA 3.3 70B</li>
  <li>Enabling offline-first resilience with local Room DB alongside Supabase cloud synchronization</li>
</ul>

<hr/>

<h2>✨ Key Features</h2>

<ul>
  <li>🤖 <b>Real-time GenAI Forecasting</b> — Sub-second travel demand insights tailored to the guide's base city via Groq LLaMA 3.3 70B</li>
  <li>🗺️ <b>Smart Route Optimizer</b> — AI-generated crowd-beating schedules, storytelling talking points, and local culinary refreshment stops</li>
  <li>⚡ <b>AI Quick Reply Assistant</b> — Instant contextual reply generator for seamless guide-traveller communication</li>
  <li>🧳 <b>Dual-Role Marketplace</b> — Full traveller and guide flows with dynamic trip creation, location matching, and Accept/Reject actions</li>
  <li>💬 <b>Location & Acceptance Filtered Chat</b> — Active chat threads generated exclusively for travellers whose requests are accepted by the guide</li>
  <li>⭐ <b>Verified Traveller Reviews</b> — 1-to-5 star rating and feedback system dynamically updating the guide's dashboard</li>
  <li>🖼️ <b>Gallery Photo Picker</b> — Native Android visual media picker (<code>ActivityResultContracts.PickVisualMedia</code>) for profile photo management</li>
  <li>⚙️ <b>Reactive Preferences</b> — Guide operating city, hourly rate, languages, and emergency helpline persisted via DataStore & SharedPreferences</li>
  <li>📱 <b>Modern Jetpack Compose UI</b> — Built with Material 3, spring animations (<code>animateFloatAsState</code>), and bounce click effects</li>
</ul>

<hr/>

<h2>📊 Impact</h2>

<ul>
  <li>⚡ Achieved <b>sub-second AI inference times (&lt;300ms)</b> using Groq LLaMA 3.3 70B instead of standard slow endpoints</li>
  <li>🔄 Reduced UI recompositions using <b>StateFlow-based unidirectional state management</b></li>
  <li>💾 Enabled <b>hybrid persistence</b> across app restarts using Room SQLite for offline trips and Supabase for cloud sync</li>
  <li>🧠 Improved guide efficiency through <b>location-aware prompt engineering</b> injecting guide city and parameters</li>
  <li>📱 Delivered a <b>fully responsive edge-to-edge UI</b> with dark/light themes, keyboard insets, and smooth scrolling</li>
</ul>

<hr/>

<h2>🔥 Key Takeaway</h2>

<p>
Voyexa demonstrates how modern Android development, cutting-edge AI inference APIs, and cloud backend systems can be combined to build scalable, real-time marketplace applications with clean architecture and robust offline capabilities.
</p>

<hr/>

<h2>🏗️ Architecture</h2>

<p><b>Clean Architecture + MVVM</b> with strict layer separation:</p>

<ul>
  <li><b>UI Layer</b> → 100% Jetpack Compose screens (GuideDashboard, ManageBookings, ChatScreen, SettingsScreen, ProfileScreen)</li>
  <li><b>ViewModel Layer</b> → StateFlow + Coroutines for reactive state emission</li>
  <li><b>Domain Layer</b> → Pure Kotlin models (BookingItem, ReviewItem, TourScheduleItem, AppSettings)</li>
  <li><b>Data Layer (Local)</b> → Room SQLite Database + Preferences DataStore + EncryptedSharedPreferences</li>
  <li><b>Data Layer (Cloud)</b> → Supabase (Auth, PostgREST PostgreSQL, Realtime Subscriptions)</li>
  <li><b>AI Engine</b> → Groq API with LLaMA 3.3 70B parameter model</li>
</ul>

<hr/>

<h2>🔄 Application Flow</h2>

<ol>
  <li>User registers/logs in as Traveller or Guide via Supabase Auth</li>
  <li>Traveller creates a trip specifying destination, dates, and package type</li>
  <li>Trip automatically registers on local guides' dashboards matching that location</li>
  <li>Guide reviews the request and chooses to <b>Accept (🟢)</b> or <b>Reject (🔴)</b></li>
  <li>Accepted requests dynamically open active chat messaging threads between traveller and guide</li>
  <li>Guide views real-time travel demand insights and route optimizations for their operating city</li>
  <li>After tour completion, traveller leaves a 1–5 star review and rating that updates guide's profile</li>
</ol>

<hr/>

<h2>🛠 Tech Stack</h2>

<ul>
  <li><b>Language:</b> Kotlin 1.9+</li>
  <li><b>UI Toolkit:</b> Jetpack Compose & Material 3</li>
  <li><b>Architecture:</b> Clean Architecture + MVVM</li>
  <li><b>Asynchronous Programming:</b> Kotlin Coroutines & StateFlow</li>
  <li><b>AI / LLM Inference:</b> Groq API (LLaMA 3.3 70B Versatile)</li>
  <li><b>Cloud Backend:</b> Supabase (GoTrue Auth, PostgREST, Realtime)</li>
  <li><b>Local Database:</b> Room SQLite Database (KSP)</li>
  <li><b>Local Preferences:</b> Jetpack Preferences DataStore & EncryptedSharedPreferences</li>
  <li><b>Networking:</b> OkHttp & Ktor Client</li>
</ul>

<hr/>
<h2>🔄 Application Flow</h2>
```mermaid
flowchart TD
    subgraph Auth["🔐 Authentication & Role Selection"]
        A["User Opens Voyexa"] --> B{"Supabase Auth<br/>(Login / Sign Up)"}
        B -->|Traveller Role| T1["Traveller Dashboard"]
        B -->|Guide Role| G1["Guide Dashboard & Setup<br/>(Operating City, Rates, Bio)"]
    end
    subgraph Matching["🗺️ Trip Creation & Location-Aware Matching"]
        T1 --> T2["Create Trip Request<br/>(Destination, Dates, Package)"]
        T2 --> DB[("Supabase Cloud Sync &<br/>Local Room SQLite Cache")]
        DB --> M{"Match Engine<br/>(Operating City)"}
        M -->|Destination Match| G2["Booking Request Appears<br/>on Guide Dashboard"]
    end
    subgraph Decision["⚡ Guide Decision & Chat Activation"]
        G2 --> D{"Guide Decision"}
        D -->|Reject 🔴| RJ["Booking Marked Rejected<br/>Traveller Notified"]
        D -->|Accept 🟢| AC["Booking Accepted<br/>Chat Channel Dynamic Unlock"]
    end
    subgraph Engagement["💬 Collaboration & GenAI Intelligence"]
        AC <--> CH["Real-Time Chat Thread<br/>(Traveller ↔ Guide)"]
        AI["Groq Cloud (LLaMA 3.3 70B)<br/>• Real-time Travel Demand Forecast<br/>• Crowd-Beating Route Optimizer<br/>• AI Quick Reply Generator"]
        AI -.->|Contextual Replies| CH
        AI -.->|City Demand Insights| G1
    end
    subgraph Review["⭐ Completion & Feedback Loop"]
        CH --> TC["Tour Completed"]
        TC --> RV["Traveller Leaves Verified Review<br/>(1–5 Star Rating & Feedback)"]
        RV --> PR["Guide Profile & Analytics<br/>Dynamically Updated (Room + Supabase)"]
    end
    classDef primary fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#0369a1;
    classDef accent fill:#f3e8ff,stroke:#9333ea,stroke-width:2px,color:#6b21a8;
    classDef success fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#15803d;
    classDef danger fill:#fee2e2,stroke:#dc2626,stroke-width:2px,color:#b91c1c;
    classDef ai fill:#fef3c7,stroke:#d97706,stroke-width:2px,color:#92400e;
    class A,B,T1,G1 primary;
    class AI ai;
    class AC success;
    class RJ danger;
    class CH,T2,G2,RV,PR accent;

<h2>📦 Setup & Installation Instructions</h2>

<p><b>Prerequisites:</b></p>
<ul>
  <li>Android Studio Ladybug / Koala or later</li>
  <li>JDK 17+</li>
  <li>Android SDK (API 24 to 35)</li>
  <li>Groq API key — <a href="https://console.groq.com">console.groq.com</a></li>
  <li>Supabase project — <a href="https://supabase.com">supabase.com</a></li>
</ul>

<p><b>Installation:</b></p>

<pre>
git clone https://github.com/debasrita15/Voyexa-Travel-Planner.git
</pre>

<p>Open the project in Android Studio, sync Gradle, and run on an Android emulator or physical device 🚀</p>

<hr/>

<h2>👥 Collaborators & Authors</h2>

<ul>
  <li>
    👩‍💻 <b>Debasrita Das</b> — <a href="https://github.com/debasrita15">@debasrita15</a>
  </li>
  <li>
    👨‍💻 <b>Swayam Subham Mishra</b> — <a href="https://github.com/SwayamK9">@SwayamK9</a>
  </li>
  <li>
    👩‍💻 <b>Esha Agarwal</b> — <a href="https://github.com/eshaagarwal1805-hash">@eshaagarwal1805-hash</a>
  </li>
  <li>
    👨‍💻 <b>G. Krushna Prasad Achary</b> — <a href="https://github.com/G-Krushna">@G-Krushna</a>
  </li>
</ul>

<hr/>

<h2>📜 License</h2>

<p>This project is licensed under the MIT License.</p>

<hr/>

<h2>⭐ Support</h2>

<p>If you find this project useful, please give it a ⭐ on GitHub!</p>
