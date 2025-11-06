export const paths = {
  home: {
    getHref: () => "/",
  },

  auth: {
    login: {
      getHref: (redirectTo?: string | null | undefined) =>
        `/login${
          redirectTo ? `?redirectTo=${encodeURIComponent(redirectTo)}` : ""
        }`,
    },
  },

  products: {
    list: {
      getHref: () => "/products",
    },
    detail: {
      getHref: (id: string) => `/products/${id}`,
    },
  },

  cart: {
    getHref: () => "/cart",
  },
} as const;
