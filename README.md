( Various parts of the source code are intentionally missing - thank you for your understanding. )

Used technologies: Spring Boot, jQuery, JPA, Bootstrap, CSS, HTML

This is a responsive site related to travel arrangements.

There are two types of authenticated users: a "normal" user (which means he isn't an admin) and admin. As an unauthenticated user man can see just the basic home page from where you can navigate to the login page and that's it.

You can upload products (for travel), cities (each city has a "city card" on the page) and categories, you can modify or delete them (as an admin). The categories are for products as we can classify different products into different categories.

There are 3 pages for a normal user:

1. Cities page: where you can check the uploaded cities, and as I wrote there is a "city card" for each city on which you can see details about it (picture, video and different data, sights, as you wish), so the user can easier decide whether he would like to travel to there or not. On this page there is also a filter section yet where you can filter cities.

2. Webshop page: where you can add to your cart different products from different categories which can be useful for a travel. The user can see his checkout page also where he still can modify the amout of the choosen products or he can delete them (the checkout process of course is just a demo and will not redirect to any payout page).

3. Contact page: which is also just a demo and you can feel as you have sent a message. ;)

The above mentioned 3 pages are available for admins as well, of course.

As a normal user you can't see the Admin section (which contains 3 subpages: Cities, Categories, Products), but as an admin you can - on the Admin section you can upload / modify / delete products, cities and categories, just as I mentioned above.

As an admin what you can do yet:
- reordering products and cities by clicking on one of them and hold the mouse and move them up or down on the page (so it will work just on laptop, not on tablet or mobile)
- there are different properties of a city, a product or a category, and by some of these properties you can sort them by ascending or descending
- there is a sorting property for each of them (the sorting values will determine in which order you can see them on the page), and you can change this sorting value just by clicking the Edit button of a product, city or category and change this property manually (if you are on mobile it's a good alternative instead of the reordering with mouse)

For listing, showing the cities, products and categories, I use pagination.

I hope I could give you a small insight about my project. Thank you for your reading.
