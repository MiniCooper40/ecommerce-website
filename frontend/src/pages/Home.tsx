const Home = () => {
  return (
    <div className="space-y-12">
      <section className="text-center py-12">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          Welcome to EcommerceApp
        </h1>
        <p className="text-xl text-gray-600 mb-8">
          Discover amazing products at unbeatable prices
        </p>
        <button className="btn btn-primary text-lg px-8 py-3">
          Shop Now
        </button>
      </section>
      
      <section>
        <h2 className="text-2xl font-bold text-gray-900 mb-8">Featured Products</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {/* Product cards will be rendered here */}
          <div className="bg-white rounded-lg shadow-md p-4">
            <div className="bg-gray-200 h-48 rounded-md mb-4"></div>
            <h3 className="font-semibold text-gray-900">Sample Product</h3>
            <p className="text-gray-600">$99.99</p>
          </div>
        </div>
      </section>
    </div>
  )
}

export default Home